import type { CourseResponseDTO } from "../types";
import { handleResponse } from './apiUtil'

const BASE_URL = import.meta.env.VITE_API_URL //?? 'http://localhost:8080'    // Default to localhost if env variable is not set

export async function getCourseByIdAndUser(canvasCourseId: number, userId: number): Promise<CourseResponseDTO> {
    const response = await fetch(`${BASE_URL}/api/v1/courses/${canvasCourseId}/users/${userId}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        }
    })  // wait for backend to response when fetching user info
    return handleResponse<CourseResponseDTO>(response)
}

export async function updateAssignments(canvasCourseId: number, userId: number): Promise<CourseResponseDTO> {
    const response = await fetch(`${BASE_URL}/api/v1/courses/${canvasCourseId}/users/${userId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        }
    })  // wait for backend to response when fetching user info
    return handleResponse<CourseResponseDTO>(response)
}