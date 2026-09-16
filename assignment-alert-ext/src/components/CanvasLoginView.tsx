import { useState } from 'react'
import type { FormEvent } from 'react'
import { connectToCanvas } from '../api/canvas'
import { saveSession } from '../lib/canvasSession'
import type { CanvasSession } from '../lib/canvasSession'
import Spinner from './Spinner'
import './CanvasLoginView.css'

/*
  The login screen. Takes a Canvas URL and access token, POSTs them to
  /api/v1/canvas/connect, saves the returned session to chrome.storage, then
  calls onConnected so App.tsx switches to the calendar. Styling is in
  CanvasLoginView.css.

  Layout:
    CanvasLoginViewProps ... just onConnected, supplied by App.tsx
    normalizeDomain ........ prepends https:// when the user types a bare host
    CanvasLoginView ........ form state, handleSubmit, and the rendered form

  All the wording on this screen lives here: the title, the subtitle, the field
  labels, the input placeholders, the error text, and the button.

  Fully commented reference copy: ~/Documents/Assignment_Alert_frontend_notes/
*/

type CanvasLoginViewProps = {
    onConnected: (session: CanvasSession) => void
}

function normalizeDomain(rawDomain: string): string {
    const trimmed = rawDomain.trim().replace(/\/+$/, '')
    if (/^https?:\/\//i.test(trimmed)) {
        return trimmed
    }
    return `https://${trimmed}`
}

function CanvasLoginView({ onConnected }: CanvasLoginViewProps) {
    const [domain, setDomain] = useState('')
    const [accessToken, setAccessToken] = useState('')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        setError(null)
        setLoading(true)

        try {
            const connection = await connectToCanvas({
                domain: normalizeDomain(domain),
                accessToken: accessToken.trim(),
            })

            const session: CanvasSession = {
                userId: connection.userId,
                name: connection.name,
                email: connection.email,
                schoolDomain: connection.schoolDomain,
            }
            await saveSession(session)
            onConnected(session)
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to connect to Canvas.')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="canvas-login">
            <h1>Connect Canvas</h1>

            <p className="canvas-login-subtitle">
                Enter your institution's Canvas URL and an access token to get started.
            </p>

            <form onSubmit={handleSubmit}>
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

                {error && <p className="canvas-login-error">{error}</p>}

                <button type="submit" disabled={loading}>
                    {loading && <Spinner variant="inline" />}
                    {loading ? 'Connecting…' : 'Connect'}
                </button>
            </form>
        </div>
    )
}

export default CanvasLoginView
