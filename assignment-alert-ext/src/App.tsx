import { useEffect, useState } from 'react'
import CanvasLoginView from './components/CanvasLoginView'
import UpcomingCalendar from './components/UpcomingCalendar'
import Spinner from './components/Spinner'
import { clearSession, getStoredSession, onSessionCleared } from './lib/canvasSession'
import type { CanvasSession } from './lib/canvasSession'
import './App.css'

/*
  The root of the popup. Reads the saved Canvas session from chrome.storage and
  picks one of three screens:
    1. a Spinner, while that read is in flight
    2. CanvasLoginView, when nobody is signed in
    3. the signed-in screen — prompt line, UpcomingCalendar, Disconnect button

  handleDisconnect clears the stored session, which drops back to screen 2.
  The signed-in screen's wording lives here; its styling is in App.css.
*/

function App() {
    const [session, setSession] = useState<CanvasSession | null>(null)
    const [checkingSession, setCheckingSession] = useState(true)

    useEffect(() => {
        getStoredSession().then((stored) => {
            setSession(stored)
            setCheckingSession(false)
        })
    }, [])

    useEffect(() => onSessionCleared(() => setSession(null)), [])

    async function handleDisconnect() {
        await clearSession()
        setSession(null)
    }

    if (checkingSession) {
        return <Spinner label="Loading…" />
    }

    if (!session) {
        return <CanvasLoginView onConnected={setSession} />
    }

    return (
        <section id="connected">
            <p className="session-info">
                What to work on today?
            </p>

            <UpcomingCalendar />

            <button className="counter" onClick={handleDisconnect}>
                Disconnect
            </button>
        </section>
    )
}

export default App
