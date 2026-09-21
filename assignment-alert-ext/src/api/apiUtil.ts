import { clearSession, getStoredSession } from '../lib/canvasSession'

// How long to wait for the backend before giving up, in milliseconds.
export const DEFAULT_TIMEOUT_MS = 10_000
// Longer limit for calls that make the backend sync with Canvas (login, course refresh), which can be slow.
export const SYNC_TIMEOUT_MS = 60_000

// Turns a millisecond duration into readable text: "10 seconds", "1 minute", "5 minutes".
function formatDuration(ms: number): string {
    const seconds = Math.round(ms / 1000)
    if (seconds < 60) {
        return seconds === 1 ? '1 second' : `${seconds} seconds`
    }
    const minutes = Math.round(seconds / 60)
    return minutes === 1 ? '1 minute' : `${minutes} minutes`
}

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
            throw new Error(`Can't reach the server (no response after ${formatDuration(timeoutMs)}). Check your connection or that the server is running.`)
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

// A plain-language message for each kind of failure, used when the server doesn't send one of its own.
function statusMessage(status: number): string {
    if (status === 401) return 'Your session has expired. Please connect again.'
    if (status === 403) return 'The server refused this request.'
    if (status === 404) return 'That item could not be found.'
    if (status === 429) return 'Too many requests. Please try again later.'
    if (status >= 500) return 'Something went wrong on the server. Please try again.'
    return `The request failed (error ${status}).`
}

// Pulls the server's "message" field out of an error body, or null if there isn't a usable one.
function serverMessage(text: string | null): string | null {
    if (!text) return null
    try {
        const parsed = JSON.parse(text)
        return typeof parsed?.message === 'string' && parsed.message.trim() ? parsed.message : null
    } catch {
        return null
    }
}

// Removes any web address or API path so error text never exposes where the server lives.
function stripUrls(message: string): string {
    return message
        .replace(/https?:\/\/\S+/gi, '')
        .replace(/\/api\/\S*/gi, '')
        .replace(/\s{2,}/g, ' ')
        .trim()
}

export async function handleResponse<T>(response: Response): Promise<T> {  // Generic function to handle API responses and errors
    if (!response.ok) {     // only true if the status code is in the range 200-299, fetch considers 400 and 500 success so this is needed
        const text = await response.text().catch(() => null)
        const fromServer = serverMessage(text)
        const message = (fromServer && stripUrls(fromServer)) || statusMessage(response.status)
        throw new Error(message)
    }

    const contentType = response.headers.get('content-type') ?? ''
    if (!contentType.includes('application/json')) {
        throw new Error('The server sent a response the extension could not read.')
    }

    return response.json() as Promise<T>
}