import { useEffect, useState } from 'react'
import CanvasLoginView from './components/CanvasLoginView'
import UpcomingCalendar from './components/UpcomingCalendar'
import { clearSession, getStoredSession } from './lib/canvasSession'
import type { CanvasSession } from './lib/canvasSession'
import './App.css'

function App() {
  const [session, setSession] = useState<CanvasSession | null>(null)
  const [checkingSession, setCheckingSession] = useState(true)

  useEffect(() => {
    getStoredSession().then((stored) => {
      setSession(stored)
      setCheckingSession(false)
    })
  }, [])

  async function handleDisconnect() {
    await clearSession()
    setSession(null)
  }

  if (checkingSession) {
    return null
  }

  if (!session) {
    return <CanvasLoginView onConnected={setSession} />
  }

  return (
    <section id="connected">
      <p className="session-info">
        Signed in as <strong>{session.name}</strong> ({session.schoolDomain})
      </p>
      <UpcomingCalendar />
      <button className="counter" onClick={handleDisconnect}>
        Disconnect
      </button>
    </section>
  )
}

export default App
