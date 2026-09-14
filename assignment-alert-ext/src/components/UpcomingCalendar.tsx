import { useEffect, useMemo, useState } from 'react'
import { getUpcomingAssignments } from '../api/assignments'
import type { AssignmentResponseDTO } from '../types'
import './UpcomingCalendar.css'

// How many week rows the grid shows. 1 = just this week, 7 = seven weeks.
const TOTAL_WEEKS = 1
// Column headers, in order. Must start on Sunday to match `weekStart` below.
const DAY_LABELS = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa']

// Turns a Date into a "YYYY-MM-DD" string, used as a lookup key for each day.
// Avoid toISOString()/new Date(key) here - both convert through UTC and can
// shift the calendar date by one day depending on the viewer's timezone.
function toDateKey(date: Date): string {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
}

// Reverse of toDateKey: "YYYY-MM-DD" back into a local-time Date.
function dateFromKey(key: string): Date {
    const [year, month, day] = key.split('-').map(Number)
    return new Date(year, month - 1, day)
}

// Returns a copy of the date set to midnight, so dates can be compared by day only.
function startOfDay(date: Date): Date {
    const copy = new Date(date)
    copy.setHours(0, 0, 0, 0)
    return copy
}

function UpcomingCalendar() {
    // ---- State ----
    // Assignments returned by the backend.
    const [assignments, setAssignments] = useState<AssignmentResponseDTO[]>([])
    // True while waiting for the backend; shows "Loading calendar…".
    const [loading, setLoading] = useState(true)
    // Error message to show instead of the calendar if the request fails.
    const [error, setError] = useState<string | null>(null)

    // Today's date at midnight, computed once when the component first renders.
    const today = useMemo(() => startOfDay(new Date()), [])
    const todayKey = useMemo(() => toDateKey(today), [today])
    // Which day the user clicked. Starts on today; drives the detail list below the grid.
    const [selectedKey, setSelectedKey] = useState<string>(todayKey)

    // ---- Load data ----
    // Runs once when the calendar appears: fetch upcoming assignments from the backend.
    useEffect(() => {
        // Guards against setting state after the popup closes mid-request.
        let cancelled = false
        setLoading(true)
        setError(null)

        getUpcomingAssignments()
            .then((data) => {
                if (!cancelled) setAssignments(data)
            })
            .catch((err) => {
                if (!cancelled) {
                    setError(err instanceof Error ? err.message : 'Failed to load assignments.')
                }
            })
            .finally(() => {
                if (!cancelled) setLoading(false)
            })

        return () => {
            cancelled = true
        }
    }, [])

    // ---- Derived data (recomputed only when its inputs change) ----
    // The Sunday that starts the first row of the grid.
    const weekStart = useMemo(() => {
        const start = new Date(today)
        start.setDate(start.getDate() - start.getDay()) // back up to the Sunday of this week
        return start
    }, [today])

    // One Date per cell in the grid: TOTAL_WEEKS rows x 7 days, starting at weekStart.
    const days = useMemo(
        () =>
            Array.from({ length: TOTAL_WEEKS * 7 }, (_, i) => {
                const date = new Date(weekStart)
                date.setDate(date.getDate() + i)
                return date
            }),
        [weekStart]
    )

    // Groups assignments by due day: "2026-09-20" -> [assignment, assignment, ...].
    // Assignments with no due date are skipped since they can't go on a calendar.
    const assignmentsByDay = useMemo(() => {
        const map = new Map<string, AssignmentResponseDTO[]>()
        for (const assignment of assignments) {
            if (!assignment.dueDate) continue
            const key = toDateKey(startOfDay(new Date(assignment.dueDate)))
            const existing = map.get(key)
            if (existing) {
                existing.push(assignment)
            } else {
                map.set(key, [assignment])
            }
        }
        return map
    }, [assignments])

    // Assignments for the clicked day, shown in the list under the grid.
    const selectedAssignments = assignmentsByDay.get(selectedKey) ?? []
    const selectedDate = dateFromKey(selectedKey)

    // ---- Render ----
    // While loading or on error, show a single message instead of the calendar.
    if (loading) {
        return <p className="calendar-status">Loading calendar…</p>
    }

    if (error) {
        return <p className="calendar-status calendar-error">{error}</p>
    }

    return (
        <div className="upcoming-calendar">
            {/* Header row: Su Mo Tu ... */}
            <div className="calendar-day-labels">
                {DAY_LABELS.map((label, i) => (
                    <span key={i}>{label}</span>
                ))}
            </div>

            {/* The grid of clickable day cells. */}
            <div className="calendar-grid">
                {days.map((date) => {
                    const key = toDateKey(date)
                    const dayAssignments = assignmentsByDay.get(key) ?? []
                    const isToday = key === todayKey
                    const isPast = date < today
                    const isSelected = key === selectedKey

                    return (
                        <button
                            key={key}
                            type="button"
                            // Builds the class list for this cell, e.g. "calendar-day is-today has-due".
                            // Each extra class maps to a style in UpcomingCalendar.css.
                            className={[
                                'calendar-day',
                                isToday && 'is-today',
                                isPast && 'is-past',
                                isSelected && 'is-selected',
                                dayAssignments.length > 0 && 'has-due',
                            ]
                                .filter(Boolean)
                                .join(' ')}
                            onClick={() => setSelectedKey(key)}
                        >
                            {/* Day of the month, e.g. 14 */}
                            <span className="calendar-day-number">{date.getDate()}</span>
                            {/* Small badge in the corner with the number of assignments due */}
                            {dayAssignments.length > 0 && (
                                <span className="calendar-day-count">{dayAssignments.length}</span>
                            )}
                        </button>
                    )
                })}
            </div>

            {/* Details panel under the grid for whichever day is selected. */}
            <div className="calendar-day-detail">
                {/* Heading like "Monday, Sep 14". Change the options to format it differently. */}
                <h3>
                    {selectedDate.toLocaleDateString(undefined, {
                        weekday: 'long',
                        month: 'short',
                        day: 'numeric',
                    })}
                </h3>
                {selectedAssignments.length === 0 ? (
                    <p className="calendar-empty">Nothing due.</p>
                ) : (
                    <ul className="calendar-assignment-list">
                        {selectedAssignments.map((assignment) => (
                            <li key={assignment.assignmentId}>
                                <strong>{assignment.assignmentName}</strong>
                                <span className="calendar-course">{assignment.courseName}</span>
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </div>
    )
}

export default UpcomingCalendar
