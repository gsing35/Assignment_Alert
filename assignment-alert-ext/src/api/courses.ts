import type { CourseResponseDTO } from "../types";
import { fetchWithTimeout, handleResponse, SYNC_TIMEOUT_MS } from './apiUtil'

const BASE_URL = import.meta.env.VITE_API_URL //?? 'http://localhost:8080'    // Default to localhost if env variable is not set

export async function getCourse(canvasCourseId: number): Promise<CourseResponseDTO> {
    const response = await fetchWithTimeout(`${BASE_URL}/api/v1/courses/${canvasCourseId}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        }
    })  // wait for backend to response when fetching user info
    return handleResponse<CourseResponseDTO>(response)
}

export async function updateAssignments(canvasCourseId: number): Promise<CourseResponseDTO> {
    const response = await fetchWithTimeout(`${BASE_URL}/api/v1/courses/${canvasCourseId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        }
    }, SYNC_TIMEOUT_MS)  // triggers a Canvas re-sync for the course, which can take a while
    return handleResponse<CourseResponseDTO>(response)
}
