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