import { useState } from 'react'
import type { FormEvent } from 'react'
import { connectToCanvas } from '../api/canvas'
import { saveSession } from '../lib/canvasSession'
import type { CanvasSession } from '../lib/canvasSession'
import './CanvasLoginView.css'

// Props passed in from App.tsx. `onConnected` is called after a successful login
// so App can switch from this login screen to the calendar screen.
type CanvasLoginViewProps = {
    onConnected: (session: CanvasSession) => void
}

// Backend expects `domain` as a full URL (e.g. "https://canvas.vt.edu"), but
// users naturally type just the host, so add the scheme if it's missing.
function normalizeDomain(rawDomain: string): string {
    const trimmed = rawDomain.trim().replace(/\/+$/, '')
    if (/^https?:\/\//i.test(trimmed)) {
        return trimmed
    }
    return `https://${trimmed}`
}

function CanvasLoginView({ onConnected }: CanvasLoginViewProps) {
    // ---- State ----
    // What the user has typed into each input.
    const [domain, setDomain] = useState('')
    const [accessToken, setAccessToken] = useState('')
    // True while the connect request is in progress; disables the form.
    const [loading, setLoading] = useState(false)
    // Error message shown above the Connect button (e.g. invalid token).
    const [error, setError] = useState<string | null>(null)

    // Runs when the form is submitted (Connect button or pressing Enter).
    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault() // stop the browser from reloading the popup
        setError(null)
        setLoading(true)

        try {
            // POST /api/v1/canvas/connect: backend validates the token and syncs courses.
            const connection = await connectToCanvas({
                domain: normalizeDomain(domain),
                accessToken: accessToken.trim(),
            })

            // Save who is logged in to chrome.storage so the popup remembers them next time.
            const session: CanvasSession = {
                userId: connection.userId,
                name: connection.name,
                email: connection.email,
                schoolDomain: connection.schoolDomain,
            }
            await saveSession(session)
            onConnected(session)
        } catch (err) {
            // Backend or network error: show the message in the form.
            setError(err instanceof Error ? err.message : 'Failed to connect to Canvas.')
        } finally {
            setLoading(false)
        }
    }

    return (
        // Outer wrapper; styled by .canvas-login in CanvasLoginView.css.
        <div className="canvas-login">
            <h1>Connect Canvas</h1>
            <p className="canvas-login-subtitle">
                Enter your institution's Canvas URL and an access token to get started.
            </p>

            <form onSubmit={handleSubmit}>
                {/* School URL input. htmlFor links the label to the input's id. */}
                <label htmlFor="domain">Institution URL</label>
                <input
                    id="domain"
                    type="text"
                    placeholder="canvas.vt.edu"
                    value={domain}
                    onChange={(e) => setDomain(e.target.value)}
                    disabled={loading}
                    autoComplete="off"
                    required
                />

                {/* Token input. type="password" hides the characters as they're typed. */}
                <label htmlFor="accessToken">Canvas Access Token</label>
                <input
                    id="accessToken"
                    type="password"
                    placeholder="Paste your access token"
                    value={accessToken}
                    onChange={(e) => setAccessToken(e.target.value)}
                    disabled={loading}
                    autoComplete="off"
                    required
                />

                {/* Only rendered when there's an error. */}
                {error && <p className="canvas-login-error">{error}</p>}

                {/* Button text switches while the request is running. */}
                <button type="submit" disabled={loading}>
                    {loading ? 'Connecting…' : 'Connect'}
                </button>
            </form>
        </div>
    )
}

export default CanvasLoginView
