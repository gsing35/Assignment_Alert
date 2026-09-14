import { useEffect, useMemo, useState } from 'react'
import { getUpcomingAssignments } from '../api/assignments'
import type { AssignmentResponseDTO } from '../types'
import './UpcomingCalendar.css'

const TOTAL_WEEKS = 7
const DAY_LABELS = ['S', 'M', 'T', 'W', 'T', 'F', 'S']

// Avoid toISOString()/new Date(key) here - both convert through UTC and can
// shift the calendar date by one day depending on the viewer's timezone.
function toDateKey(date: Date): string {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
}

function dateFromKey(key: string): Date {
    const [year, month, day] = key.split('-').map(Number)
    return new Date(year, month - 1, day)
}

function startOfDay(date: Date): Date {
    const copy = new Date(date)
    copy.setHours(0, 0, 0, 0)
    return copy
}

function UpcomingCalendar() {
    const [assignments, setAssignments] = useState<AssignmentResponseDTO[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const today = useMemo(() => startOfDay(new Date()), [])
    const todayKey = useMemo(() => toDateKey(today), [today])
    const [selectedKey, setSelectedKey] = useState<string>(todayKey)

    useEffect(() => {
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

    const weekStart = useMemo(() => {
        const start = new Date(today)
        start.setDate(start.getDate() - start.getDay()) // back up to the Sunday of this week
        return start
    }, [today])

    const days = useMemo(
        () =>
            Array.from({ length: TOTAL_WEEKS * 7 }, (_, i) => {
                const date = new Date(weekStart)
                date.setDate(date.getDate() + i)
                return date
            }),
        [weekStart]
    )

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

    const selectedAssignments = assignmentsByDay.get(selectedKey) ?? []
    const selectedDate = dateFromKey(selectedKey)

    if (loading) {
        return <p className="calendar-status">Loading calendar…</p>
    }

    if (error) {
        return <p className="calendar-status calendar-error">{error}</p>
    }

    return (
        <div className="upcoming-calendar">
            <div className="calendar-day-labels">
                {DAY_LABELS.map((label, i) => (
                    <span key={i}>{label}</span>
                ))}
            </div>

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
                            <span className="calendar-day-number">{date.getDate()}</span>
                            {dayAssignments.length > 0 && (
                                <span className="calendar-day-count">{dayAssignments.length}</span>
                            )}
                        </button>
                    )
                })}
            </div>

            <div className="calendar-day-detail">
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
