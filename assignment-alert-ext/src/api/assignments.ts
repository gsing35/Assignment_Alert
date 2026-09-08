import type { AssignmentResponseDTO, AssignmentUpdateRequest, Priority } from '../types'
import { handleResponse } from './apiUtil'

const BASE_URL = import.meta.env.VITE_API_URL //?? 'http://localhost:8080'    // Default to localhost if env variable is not set

export async function getUpcomingAssignments(): Promise<AssignmentResponseDTO[]> {
    const response = await fetch(`${BASE_URL}/api/v1/assignments?filter=upcoming`)  // wait for backend to response when fetching upcoming assignments
    return handleResponse<AssignmentResponseDTO[]>(response)
}

export async function getIncompleteAssignments(): Promise<AssignmentResponseDTO[]> {
    const response = await fetch(`${BASE_URL}/api/v1/assignments?filter=incomplete`)    // wait for backend to response when fetching upcoming assignments
    return handleResponse<AssignmentResponseDTO[]>(response)
}

export async function getBlockingAssignments(): Promise<AssignmentResponseDTO[]> {
    const response = await fetch(`${BASE_URL}/api/v1/assignments?filter=blocking`)  // wait for backend to response when fetching upcoming assignments
    return handleResponse<AssignmentResponseDTO[]>(response)
}

export async function getAssignmentsByCourse(courseId: number): Promise<AssignmentResponseDTO[]> {
    const response = await fetch(`${BASE_URL}/api/v1/assignments?courseId=${courseId}`) // wait for backend to response when fetching upcoming assignments
    return handleResponse<AssignmentResponseDTO[]>(response)
}

export async function getAssignmentById(canvasAssignmentId: number): Promise<AssignmentResponseDTO> {
    const response = await fetch(`${BASE_URL}/api/v1/assignments/${canvasAssignmentId}`)
    return handleResponse<AssignmentResponseDTO>(response)
}

export async function updateAssignment(
    assignmentId: number,
    updateRequest: AssignmentUpdateRequest
): Promise<AssignmentResponseDTO> {
    const response = await fetch(`${BASE_URL}/api/v1/assignments/${assignmentId}`, {
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
    const response = await fetch(
        `${BASE_URL}/api/v1/assignments/${assignmentId}/completed?completed=${completed}`,
        { method: 'PUT' }   // Similar to the previous put but this endpoint only updates the completed status of the assignment, so we don't need to send a body with the request, we can just use the query parameter to indicate whether the assignment is completed or not
    )   // No content type body needed because there is no @RequestBody and @Requestpararm goes in the url 
    return handleResponse<AssignmentResponseDTO>(response)
}

export async function getAssignmentsByPriority(priority: Priority): Promise<AssignmentResponseDTO[]> {
    const response = await fetch(`${BASE_URL}/api/v1/assignments?priority=${priority}`)
    return handleResponse<AssignmentResponseDTO[]>(response)
}