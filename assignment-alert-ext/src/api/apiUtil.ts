import { clearSession, getStoredSession } from '../lib/canvasSession'

// How long to wait for the backend before giving up, in milliseconds.
export const DEFAULT_TIMEOUT_MS = 10_000
// Longer limit for calls that make the backend sync with Canvas (login, course refresh), which can be slow.
export const SYNC_TIMEOUT_MS = 60_000

// Same as fetch(), but gives up after timeoutMs and turns network failures into readable errors,
// so the UI shows a message instead of loading forever when the server is unreachable.
export async function fetchWithTimeout(url: string, init: RequestInit = {}, timeoutMs = DEFAULT_TIMEOUT_MS): Promise<Response> {
    const session = await getStoredSession()
    const headers = new Headers(init.headers)
    if (session && !headers.has('Authorization')) {
        headers.set('Authorization', `Bearer ${session.sessionToken}`)
    }

    let response: Response
    try {
        response = await fetch(url, { ...init, headers, signal: AbortSignal.timeout(timeoutMs) })
    } catch (err) {
        if (err instanceof DOMException && err.name === 'TimeoutError') {
            throw new Error(`Can't reach the server (no response after ${timeoutMs / 1000}s). Check your connection or that the server is running.`)
        }
        if (err instanceof TypeError) {     // fetch throws TypeError when the request can't be sent at all (server down, blocked, no internet)
            throw new Error("Can't reach the server. Check your connection or that the server is running.")
        }
        throw err
    }

    if (response.status === 401 && session) {
        await clearSession()
    }

    return response
}

export async function handleResponse<T>(response: Response): Promise<T> {  // Generic function to handle API responses and errors
    if (!response.ok) {     // only true if the status code is in the range 200-299, fetch considers 400 and 500 success so this is needed
        const text = await response.text().catch(() => null)
        let message = `HTTP error: ${response.status}`

        if (text) {
            try {
                const error = JSON.parse(text)
                message = error?.message ?? JSON.stringify(error)
            } catch {
                message = `HTTP error: ${response.status} - ${text}`
            }
        }

        throw new Error(message)
    }

    const contentType = response.headers.get('content-type') ?? ''
    if (!contentType.includes('application/json')) {
        const text = await response.text().catch(() => null)
        throw new Error(`Expected JSON response but got: ${text ?? 'empty response'}`)
    }

    return response.json() as Promise<T>
}