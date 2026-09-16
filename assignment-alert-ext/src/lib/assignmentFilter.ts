import {
    getUpcomingAssignments,
    getIncompleteAssignments,
    getBlockingAssignments,
    getAssignmentsByCourse,
    getAssignmentsByPriority,
} from '../api/assignments'
import type { AssignmentResponseDTO, Priority } from '../types'

/*
  One place mapping each filter choice to the backend call that serves it.

  The backend accepts courseId, priority and filter on the same endpoint but
  applies them in strict precedence — courseId wins, then priority, then filter
  (see AssignmentsController.getAssignments). Combining them is therefore
  impossible, so a filter here is a single tagged choice rather than a set of
  independent toggles: whatever the UI shows is exactly what the server did.

  Encoded as a string ("filter:upcoming", "priority:HIGH", "course:177440") so a
  radio group and a <select> can both drive the same piece of state.
*/

export const SHOW_FILTERS = ['upcoming', 'incomplete', 'blocking'] as const
export type ShowFilter = (typeof SHOW_FILTERS)[number]

export const PRIORITIES: Priority[] = ['HIGH', 'MEDIUM', 'LOW', 'DONE']

export const DEFAULT_FILTER = 'filter:upcoming'

// Human-readable text for the collapsed "Filter options" button.
export function filterLabel(value: string, courseNames: Map<number, string>): string {
    const [kind, rest] = splitValue(value)

    if (kind === 'priority') return `${rest.charAt(0)}${rest.slice(1).toLowerCase()} priority`
    if (kind === 'course') return courseNames.get(Number(rest)) ?? 'Course'

    if (rest === 'incomplete') return 'Incomplete'
    if (rest === 'blocking') return 'Blocking'
    return 'Upcoming'
}

// True when the choice can include work whose due date has already passed.
// "upcoming" and "blocking" are both filtered to dueAt > now by the backend;
// the others are not, and the calendar cannot scroll backwards to reach them.
export function includesPastWork(value: string): boolean {
    const [kind, rest] = splitValue(value)
    if (kind === 'priority' || kind === 'course') return true
    return rest === 'incomplete'
}

export async function fetchAssignments(value: string): Promise<AssignmentResponseDTO[]> {
    const [kind, rest] = splitValue(value)

    if (kind === 'priority') {
        return getAssignmentsByPriority(rest as Priority)
    }

    if (kind === 'course') {
        // The backend's courseId parameter is matched against canvasCourseId
        // (findByCanvasCourseIdAndCourse_User), not the local primary key.
        return getAssignmentsByCourse(Number(rest))
    }

    if (rest === 'incomplete') return getIncompleteAssignments()
    if (rest === 'blocking') return getBlockingAssignments()
    return getUpcomingAssignments()
}

function splitValue(value: string): [string, string] {
    const index = value.indexOf(':')
    if (index === -1) return ['filter', 'upcoming']
    return [value.slice(0, index), value.slice(index + 1)]
}
