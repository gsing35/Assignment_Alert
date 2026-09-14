import type { CanvasAuthenticationRequest, CanvasConnectionDTO } from "../types";
import { fetchWithTimeout, handleResponse, SYNC_TIMEOUT_MS } from './apiUtil'

const BASE_URL = import.meta.env.VITE_API_URL //?? 'http://localhost:8080'    // Default to localhost if env variable is not set

export async function connectToCanvas(authRequest: CanvasAuthenticationRequest): Promise<CanvasConnectionDTO> {
    const response = await fetchWithTimeout(`${BASE_URL}/api/v1/canvas/connect`, {
        method: 'POST',  // Otherwise fetch defaults to get
        headers: {
            'Content-Type': 'application/json'      // This tells the backend that we are sending JSON in the body of the request
        },
        body: JSON.stringify(authRequest) //fetch only takes string as body, so we need to stringify the authRequest object before sending it to the backend
    }, SYNC_TIMEOUT_MS)     // connecting runs the initial Canvas sync, which can take a while
    return handleResponse<CanvasConnectionDTO>(response)
}