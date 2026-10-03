const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1/tasks'

async function handleResponse(response) {
  if (!response.ok) {
    let message = `Request failed with status ${response.status}`
    try {
      const body = await response.json()
      if (body.message) {
        message = body.message
      }
    } catch {
      // The response had no JSON body; keep the default message.
    }
    throw new Error(message)
  }
  if (response.status === 204) {
    return null
  }
  return response.json()
}

export async function getTasks() {
  return handleResponse(await fetch(BASE_URL))
}

export async function getTask(id) {
  return handleResponse(await fetch(`${BASE_URL}/${id}`))
}

export async function createTask(task) {
  return handleResponse(
    await fetch(BASE_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(task),
    }),
  )
}

export async function updateTask(id, task) {
  return handleResponse(
    await fetch(`${BASE_URL}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(task),
    }),
  )
}

export async function deleteTask(id) {
  return handleResponse(await fetch(`${BASE_URL}/${id}`, { method: 'DELETE' }))
}
