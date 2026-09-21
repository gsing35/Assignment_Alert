import { useEffect, useMemo, useState } from 'react'
import { getCurrentUser } from '../api/users'
import { DEFAULT_FILTER, fetchAssignments, includesPastWork } from '../lib/assignmentFilter'
import type { AssignmentResponseDTO, CourseResponseDTO } from '../types'
import FilterOptions from './FilterOptions'
import Spinner from './Spinner'
import './UpcomingCalendar.css'

/*
  The calendar screen: a grid of day boxes and a list of what is due on the
  selected day. Styling is in UpcomingCalendar.css.

  Layout:
    TOTAL_WEEKS / DAY_LABELS ... how many week rows, and the column headers
    toDateKey / dateFromKey .... "YYYY-MM-DD" <-> Date, kept in local time so the
                                 calendar never shifts a day across timezones
    startOfDay ................. midnight copy of a date, for day-only comparisons
    UpcomingCalendar ........... state, the fetch, the derived grid, the render

  State: assignments + loading + error from the backend, selectedKey for the
  clicked day, and weekOffset for which week is shown (0 = current, never
  negative — Back only undoes a Next).

  Note: getUpcomingAssignments() returns only work due after right now, so past
  days are always empty.
*/

const TOTAL_WEEKS = 2

const DAY_LABELS = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa']

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

    const [weekOffset, setWeekOffset] = useState(0)

    // Which backend filter is active, e.g. "filter:upcoming" or "priority:HIGH".
    const [filter, setFilter] = useState<string>(DEFAULT_FILTER)
    // Courses for the filter panel's course dropdown.
    const [courses, setCourses] = useState<CourseResponseDTO[]>([])

    // Refetches whenever the filter changes, so each choice hits its own endpoint.
    useEffect(() => {
        let cancelled = false
        setLoading(true)
        setError(null)

        fetchAssignments(filter)
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
    }, [filter])

    // Loaded once. A failure here only costs the course dropdown its options,
    // so it is swallowed rather than replacing the whole calendar with an error.
    useEffect(() => {
        let cancelled = false

        getCurrentUser()
            .then((user) => {
                if (!cancelled) setCourses(user.courses ?? [])
            })
            .catch(() => {
                if (!cancelled) setCourses([])
            })

        return () => {
            cancelled = true
        }
    }, [])

    const weekStart = useMemo(() => {
        const start = new Date(today)
        start.setDate(start.getDate() - start.getDay())
        start.setDate(start.getDate() + weekOffset * 7)
        return start
    }, [today, weekOffset])

    const isCurrentWeek = weekOffset === 0

    function changeWeek(delta: number) {
        const nextOffset = Math.max(0, weekOffset + delta)
        if (nextOffset === weekOffset) return

        setWeekOffset(nextOffset)

        if (nextOffset === 0) {
            setSelectedKey(todayKey)
            return
        }

        const firstDayOfNewWeek = new Date(today)
        firstDayOfNewWeek.setDate(firstDayOfNewWeek.getDate() - firstDayOfNewWeek.getDay())
        firstDayOfNewWeek.setDate(firstDayOfNewWeek.getDate() + nextOffset * 7)
        setSelectedKey(toDateKey(firstDayOfNewWeek))
    }

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

    // Fetched items that are already overdue, and so sit before the earliest week
    // the calendar can show. Only non-empty for filters that return past work.
    const overdueCount = useMemo(
        () =>
            assignments.filter(
                (a) => a.dueDate && startOfDay(new Date(a.dueDate)) < today
            ).length,
        [assignments, today]
    )

    // The filter box is rendered before these checks rather than after, so it
    // stays on screen while a filter change is loading. Returning early here
    // would unmount it and take away the control you just used.
    const filterBox = <FilterOptions value={filter} onChange={setFilter} courses={courses} />

    if (loading) {
        return (
            <div className="upcoming-calendar">
                {filterBox}
                <Spinner label="Loading calendar…" />
            </div>
        )
    }

    if (error) {
        return (
            <div className="upcoming-calendar">
                {filterBox}
                <p className="calendar-status calendar-error">{error}</p>
            </div>
        )
    }

    return (
        <div className="upcoming-calendar">
            {filterBox}

            {/*
              These filters are not limited to future work, but the calendar only
              travels forwards, so anything already overdue is in `assignments`
              without being reachable. Saying so beats looking empty for no reason.
            */}
            {includesPastWork(filter) && overdueCount > 0 && (
                <p className="calendar-notice">
                    {overdueCount} overdue {overdueCount === 1 ? 'item is' : 'items are'} hidden —
                    the calendar only shows today onwards.
                </p>
            )}

            <div className="calendar-nav">
                <button
                    type="button"
                    className="calendar-nav-button"
                    onClick={() => changeWeek(-1)}
                    disabled={isCurrentWeek}
                >
                    ‹ Back
                </button>

                <span className="calendar-nav-label">
                    {days[0].toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}
                    {' – '}
                    {days[days.length - 1].toLocaleDateString(undefined, {
                        month: 'short',
                        day: 'numeric',
                    })}
                </span>

                <button type="button" className="calendar-nav-button" onClick={() => changeWeek(1)}>
                    Next ›
                </button>
            </div>

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
                    // Any HIGH-priority work that day earns a "!" on the badge.
                    const hasUrgent = dayAssignments.some((a) => a.priority === 'HIGH')

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
                                <span
                                    className={[
                                        'calendar-day-count',
                                        hasUrgent && 'is-urgent',
                                    ]
                                        .filter(Boolean)
                                        .join(' ')}
                                >
                                    {dayAssignments.length}
                                    {hasUrgent && '!'}
                                </span>
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
                    <p className="calendar-empty">Nothing Due.</p>
                ) : (
                    <ul className="calendar-assignment-list">
                        {selectedAssignments.map((assignment) => (
                            <li key={assignment.assignmentId}>
                                <a
                                    className="calendar-assignment-link"
                                    href={assignment.url}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                >
                                    {assignment.assignmentName}
                                </a>
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
