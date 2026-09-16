import { useState } from 'react'
import type { CourseResponseDTO } from '../types'
import { PRIORITIES, SHOW_FILTERS, filterLabel } from '../lib/assignmentFilter'
import './FilterOptions.css'

/*
  The "Filter options" box in the top-left of the calendar.

  Collapsed it is a single button showing the active filter; clicking it opens a
  panel of choices. Every choice is one radio in the SAME group, because the
  backend applies only one filter at a time (see lib/assignmentFilter.ts) —
  making them mutually exclusive keeps the UI honest about that.

  The component holds no filter state of its own; UpcomingCalendar owns `value`
  and refetches whenever onChange fires.
*/

type FilterOptionsProps = {
    value: string
    onChange: (value: string) => void
    courses: CourseResponseDTO[]
}

const SHOW_LABELS: Record<string, string> = {
    upcoming: 'Upcoming',
    incomplete: 'Incomplete',
    blocking: 'Blocking',
}

function FilterOptions({ value, onChange, courses }: FilterOptionsProps) {
    const [open, setOpen] = useState(false)

    const courseNames = new Map(courses.map((c) => [c.canvasCourseId, c.courseName]))
    const selectedCourse = value.startsWith('course:') ? value.slice('course:'.length) : ''

    function choose(next: string) {
        onChange(next)
        setOpen(false)
    }

    return (
        <div className="filter-options">
            <button
                type="button"
                className="filter-options-toggle"
                onClick={() => setOpen(!open)}
                aria-expanded={open}
            >
                Filter options
                <span className="filter-options-current">{filterLabel(value, courseNames)}</span>
            </button>

            {open && (
                <div className="filter-options-panel">
                    <p className="filter-options-group-label">Show</p>
                    {SHOW_FILTERS.map((name) => (
                        <label key={name} className="filter-options-choice">
                            <input
                                type="radio"
                                name="assignment-filter"
                                checked={value === `filter:${name}`}
                                onChange={() => choose(`filter:${name}`)}
                            />
                            {SHOW_LABELS[name]}
                        </label>
                    ))}

                    <p className="filter-options-group-label">Priority</p>
                    {PRIORITIES.map((priority) => (
                        <label key={priority} className="filter-options-choice">
                            <input
                                type="radio"
                                name="assignment-filter"
                                checked={value === `priority:${priority}`}
                                onChange={() => choose(`priority:${priority}`)}
                            />
                            {priority.charAt(0)}
                            {priority.slice(1).toLowerCase()}
                        </label>
                    ))}

                    <p className="filter-options-group-label">Course</p>
                    <select
                        className="filter-options-select"
                        value={selectedCourse}
                        onChange={(e) => e.target.value && choose(`course:${e.target.value}`)}
                    >
                        <option value="">Choose a course…</option>
                        {courses.map((course) => (
                            <option key={course.canvasCourseId} value={course.canvasCourseId}>
                                {course.courseName?.trim() || `Untitled (${course.canvasCourseId})`}
                            </option>
                        ))}
                    </select>
                </div>
            )}
        </div>
    )
}

export default FilterOptions
