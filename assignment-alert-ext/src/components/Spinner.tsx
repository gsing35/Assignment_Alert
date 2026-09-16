/*
  The loading animation, reused wherever the popup waits on the backend:
  App.tsx (session check), UpcomingCalendar.tsx (fetching assignments), and
  CanvasLoginView.tsx (inside the Connect button).

  Two variants:
    'block'  — large centered ring with an optional label under it (default)
    'inline' — small bare ring meant to sit next to text inside a button

  The ring's size, speed and colors are in index.css under .spinner.

  Fully commented reference copy: ~/Documents/Assignment_Alert_frontend_notes/
*/

type SpinnerProps = {
    label?: string
    variant?: 'block' | 'inline'
}

function Spinner({ label, variant = 'block' }: SpinnerProps) {
    if (variant === 'inline') {
        return <span className="spinner is-inline" aria-hidden="true" />
    }

    return (
        <div className="spinner-block" role="status">
            <span className="spinner" aria-hidden="true" />
            {label && <p className="spinner-label">{label}</p>}
        </div>
    )
}

export default Spinner
