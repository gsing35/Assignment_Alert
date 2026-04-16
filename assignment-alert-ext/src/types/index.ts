/*
    Front end only needs types for what it gets from backend
    Not the records that the backend uses to parse data from the api
*/

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'DONE';

// What backend returns from /api/v1/assignments
export interface AssignmentResponseDTO {
    assigmentId: number
    assignmentName: string
    canvasAssignmentId: number
    courseName: string
    courseId: number
    dueDate: string
    createdDate: string
    completed: boolean | null // because they are nullable in the db
    pointsWorth: number | null
    grade: number
    priority: Priority
    url: string
    blockingEnabled: boolean
    hoursUntilDue: number
}

// What backend returns from /api/v1/canvas/connect returns
export interface CanvasConnectionDTO {
    userId: number
    name: string
    email: string
    schoolDomain: string
    message: string
}

// What backend returns from /api/v1/courses/{id}
export interface CourseResponseDTO {
    courseName: string
    canvasCourseId: number
    url: string
    assignments: AssignmentResponseDTO[] //
}

// What backend returns from /api/v1/users/{id}
export interface UserResponseDTO {
    name: string
    courses: CourseResponseDTO[]
    email: string
}

// Error response from backend
export interface ErrorResponse {
    path: string
    message: string
    statusCode: number
    localDateTime: string
}

// What is sent to /api/v1/canvas/connect
export interface CanvasAuthenticationRequest {
    domain: string
    accessToken: string
}

// What is sent to PUT /api/v1/assignments/{id}
export interface AssignmentUpdateRequest {
    completed: boolean | null
    priority: Priority | null
    blockedUntil: string | null
    blockingEnabled: boolean | null
}


