// Persists the logged-in Canvas user across popup opens using chrome.storage.local
// (the "storage" permission is already declared in manifest.json).

const STORAGE_KEY = 'canvasSession'

export type CanvasSession = {
    userId: number
    name: string
    email: string
    schoolDomain: string
    sessionToken: string
}

export async function getStoredSession(): Promise<CanvasSession | null> {
    const result = await chrome.storage.local.get(STORAGE_KEY)
    const session = result[STORAGE_KEY] as CanvasSession | undefined
    if (!session?.sessionToken) {
        return null
    }
    return session
}

export async function saveSession(session: CanvasSession): Promise<void> {
    await chrome.storage.local.set({ [STORAGE_KEY]: session })
}

export async function clearSession(): Promise<void> {
    await chrome.storage.local.remove(STORAGE_KEY)
}

export async function requireToken(): Promise<string> {
    const session = await getStoredSession()
    if (!session) {
        throw new Error('No Canvas session found. Please log in.')
    }
    return session.sessionToken
}

export function onSessionCleared(callback: () => void): () => void {
    const listener = (changes: { [key: string]: chrome.storage.StorageChange }, areaName: string) => {
        if (areaName === 'local' && STORAGE_KEY in changes && changes[STORAGE_KEY].newValue === undefined) {
            callback()
        }
    }
    chrome.storage.onChanged.addListener(listener)
    return () => chrome.storage.onChanged.removeListener(listener)
}
