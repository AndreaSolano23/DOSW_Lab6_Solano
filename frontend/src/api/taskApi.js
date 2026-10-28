import { API_URL, authFetch, handleResponse } from './http'

const BASE_URL = import.meta.env.VITE_API_URL ?? `${API_URL}/tasks`

const JSON_HEADERS = { 'Content-Type': 'application/json' }

export async function getTasks() {
  return handleResponse(await authFetch(BASE_URL))
}

export async function getTask(id) {
  return handleResponse(await authFetch(`${BASE_URL}/${id}`))
}

export async function createTask(task) {
  return handleResponse(
    await authFetch(BASE_URL, {
      method: 'POST',
      headers: JSON_HEADERS,
      body: JSON.stringify(task),
    }),
  )
}

export async function updateTask(id, task) {
  return handleResponse(
    await authFetch(`${BASE_URL}/${id}`, {
      method: 'PUT',
      headers: JSON_HEADERS,
      body: JSON.stringify(task),
    }),
  )
}

export async function deleteTask(id) {
  return handleResponse(await authFetch(`${BASE_URL}/${id}`, { method: 'DELETE' }))
}
