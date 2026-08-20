// Persists the logged-in Canvas user across popup opens using chrome.storage.local
// (the "storage" permission is already declared in manifest.json).

const STORAGE_KEY = 'canvasSession'

export type CanvasSession = {
    userId: number
    name: string
    email: string
    schoolDomain: string
}

export async function getStoredSession(): Promise<CanvasSession | null> {
    const result = await chrome.storage.local.get(STORAGE_KEY)
    return (result[STORAGE_KEY] as CanvasSession | undefined) ?? null
}

export async function saveSession(session: CanvasSession): Promise<void> {
    await chrome.storage.local.set({ [STORAGE_KEY]: session })
}

export async function clearSession(): Promise<void> {
    await chrome.storage.local.remove(STORAGE_KEY)
}
