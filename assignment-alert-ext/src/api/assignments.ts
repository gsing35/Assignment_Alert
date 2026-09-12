import type { AssignmentResponseDTO, AssignmentUpdateRequest, Priority } from '../types'
import { handleResponse } from './apiUtil'
import { requireUserId } from '../lib/canvasSession'

const BASE_URL = import.meta.env.VITE_API_URL //?? 'http://localhost:8080'    // Default to localhost if env variable is not set

export async function getUpcomingAssignments(): Promise<AssignmentResponseDTO[]> {
    const userId = await requireUserId()
    const response = await fetch(`${BASE_URL}/api/v1/assignments?userId=${userId}&filter=upcoming`)  // wait for backend to response when fetching upcoming assignments
    return handleResponse<AssignmentResponseDTO[]>(response)
}

export async function getIncompleteAssignments(): Promise<AssignmentResponseDTO[]> {
    const userId = await requireUserId()
    const response = await fetch(`${BASE_URL}/api/v1/assignments?userId=${userId}&filter=incomplete`)    // wait for backend to response when fetching upcoming assignments
    return handleResponse<AssignmentResponseDTO[]>(response)
}

export async function getBlockingAssignments(): Promise<AssignmentResponseDTO[]> {
    const userId = await requireUserId()
    const response = await fetch(`${BASE_URL}/api/v1/assignments?userId=${userId}&filter=blocking`)  // wait for backend to response when fetching upcoming assignments
    return handleResponse<AssignmentResponseDTO[]>(response)
}

export async function getAssignmentsByCourse(courseId: number): Promise<AssignmentResponseDTO[]> {
    const userId = await requireUserId()
    const response = await fetch(`${BASE_URL}/api/v1/assignments?userId=${userId}&courseId=${courseId}`) // wait for backend to response when fetching upcoming assignments
    return handleResponse<AssignmentResponseDTO[]>(response)
}

// assignmentId is the local database id (AssignmentResponseDTO.assignmentId), not the Canvas one
export async function getAssignmentById(assignmentId: number): Promise<AssignmentResponseDTO> {
    const userId = await requireUserId()
    const response = await fetch(`${BASE_URL}/api/v1/assignments/${assignmentId}?userId=${userId}`)
    return handleResponse<AssignmentResponseDTO>(response)
}

export async function updateAssignment(
    assignmentId: number,
    updateRequest: AssignmentUpdateRequest
): Promise<AssignmentResponseDTO> {
    const userId = await requireUserId()
    const response = await fetch(`${BASE_URL}/api/v1/assignments/${assignmentId}?userId=${userId}`, {
        method: 'PUT',  // Otherwise fetch defaults to get
        headers: {
            'Content-Type': 'application/json'      // This tells the backend that we are sending JSON in the body of the request
        },
        body: JSON.stringify(updateRequest) //fetch only takes string as body, so we need to stringify the updateRequest object before sending it to the backend
    })
    return handleResponse<AssignmentResponseDTO>(response)
}

export async function markAsCompleted(
    assignmentId: number,
    completed: boolean
): Promise<AssignmentResponseDTO> {
    const userId = await requireUserId()
    const response = await fetch(
        `${BASE_URL}/api/v1/assignments/${assignmentId}/completed?completed=${completed}&userId=${userId}`,
        { method: 'PUT' }   // Similar to the previous put but this endpoint only updates the completed status of the assignment, so we don't need to send a body with the request, we can just use the query parameter to indicate whether the assignment is completed or not
    )   // No content type body needed because there is no @RequestBody and @Requestpararm goes in the url 
    return handleResponse<AssignmentResponseDTO>(response)
}

export async function getAssignmentsByPriority(priority: Priority): Promise<AssignmentResponseDTO[]> {
    const userId = await requireUserId()
    const response = await fetch(`${BASE_URL}/api/v1/assignments?priority=${priority}&userId=${userId}`)  // wait for backend to response when fetching upcoming assignments
    return handleResponse<AssignmentResponseDTO[]>(response)
}