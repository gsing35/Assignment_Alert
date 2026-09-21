import type { UserResponseDTO } from "../types";
import { fetchWithTimeout, handleResponse } from './apiUtil'

const BASE_URL = import.meta.env.VITE_API_URL //?? 'http://localhost:8080'    // Default to localhost if env variable is not set

export async function getCurrentUser(): Promise<UserResponseDTO> {
    const response = await fetchWithTimeout(`${BASE_URL}/api/v1/users/me`)  // wait for backend to response when fetching user info
    return handleResponse<UserResponseDTO>(response)
}
