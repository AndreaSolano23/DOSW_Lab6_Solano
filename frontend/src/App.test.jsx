import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { setAccessToken } from './api/session'
import App from './App'

const API = 'http://localhost:8080/api/v1'

const ana = { id: 1, name: 'Ana Perez', email: 'ana@example.com', role: 'USER' }
const luis = { id: 2, name: 'Luis Gomez', email: 'luis@example.com', role: 'USER' }

const anaTask = { id: 10, title: 'Ana task', description: '', status: 'PENDING', priority: 'MEDIUM', dueDate: null }
const luisTask = { id: 20, title: 'Luis task', description: '', status: 'PENDING', priority: 'LOW', dueDate: null }

function jsonResponse(status, body) {
  return Promise.resolve({
    ok: status >= 200 && status < 300,
    status,
    json: () => Promise.resolve(body),
  })
}

// Simula el backend: el token "token-ana" pertenece a Ana y "token-luis" a Luis.
function fakeBackend({ expireTasks = false } = {}) {
  return vi.fn((url, options = {}) => {
    const method = options.method ?? 'GET'
    const auth = options.headers?.Authorization
    const body = options.body ? JSON.parse(options.body) : {}

    if (url === `${API}/auth/login` && method === 'POST') {
      if (body.password !== 'ContrasenaDemo123!') {
        return jsonResponse(401, { status: 401, message: 'Invalid credentials' })
      }
      const who = body.email === 'luis@example.com' ? 'luis' : 'ana'
      return jsonResponse(200, { tokenType: 'Bearer', accessToken: `token-${who}`, expiresIn: 1800 })
    }
    if (url === `${API}/auth/register` && method === 'POST') {
      if (body.email === 'ana@example.com') {
        return jsonResponse(409, { status: 409, message: 'Email already registered' })
      }
      return jsonResponse(201, { id: 3, name: body.name, email: body.email, role: 'USER' })
    }
    if (url === `${API}/users/me`) {
      if (auth === 'Bearer token-ana') return jsonResponse(200, ana)
      if (auth === 'Bearer token-luis') return jsonResponse(200, luis)
      return jsonResponse(401, null)
    }
    if (url === `${API}/tasks` && method === 'GET') {
      if (expireTasks) return jsonResponse(401, null)
      if (auth === 'Bearer token-ana') return jsonResponse(200, [anaTask])
      if (auth === 'Bearer token-luis') return jsonResponse(200, [luisTask])
      return jsonResponse(401, null)
    }
    return jsonResponse(404, { message: 'Not found' })
  })
}

async function logIn(user, email, password = 'ContrasenaDemo123!') {
  await user.type(screen.getByLabelText('Email'), email)
  await user.type(screen.getByLabelText('Password'), password)
  await user.click(screen.getByRole('button', { name: 'Log in' }))
}

describe('App authentication', () => {
  beforeEach(() => {
    setAccessToken(null)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('shows the login form when there is no session', () => {
    vi.stubGlobal('fetch', fakeBackend())

    render(<App />)

    expect(screen.getByRole('heading', { name: 'Log in' })).toBeInTheDocument()
    expect(screen.queryByText('Ana task')).not.toBeInTheDocument()
  })

  it('logs in, sends the token and shows the user tasks', async () => {
    const user = userEvent.setup()
    const fetchMock = fakeBackend()
    vi.stubGlobal('fetch', fetchMock)

    render(<App />)
    await logIn(user, 'ana@example.com')

    expect(await screen.findByText('Ana task')).toBeInTheDocument()
    expect(screen.getByText('Ana Perez')).toBeInTheDocument()

    const tasksCall = fetchMock.mock.calls.find(([url]) => url === `${API}/tasks`)
    expect(tasksCall[1].headers.Authorization).toBe('Bearer token-ana')
  })

  it('shows an error and no session when the password is wrong', async () => {
    const user = userEvent.setup()
    vi.stubGlobal('fetch', fakeBackend())

    render(<App />)
    await logIn(user, 'ana@example.com', 'wrong-password')

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid credentials')
    expect(screen.queryByText(/session has expired/i)).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Log out' })).not.toBeInTheDocument()
  })

  // PT-16
  it('log out removes the user and their tasks from the UI', async () => {
    const user = userEvent.setup()
    vi.stubGlobal('fetch', fakeBackend())

    render(<App />)
    await logIn(user, 'ana@example.com')
    await screen.findByText('Ana task')

    await user.click(screen.getByRole('button', { name: 'Log out' }))

    expect(screen.getByRole('heading', { name: 'Log in' })).toBeInTheDocument()
    expect(screen.queryByText('Ana task')).not.toBeInTheDocument()
    expect(screen.queryByText('Ana Perez')).not.toBeInTheDocument()
  })

  // PT-17
  it('goes back to login when a request answers 401', async () => {
    const user = userEvent.setup()
    vi.stubGlobal('fetch', fakeBackend({ expireTasks: true }))

    render(<App />)
    await logIn(user, 'ana@example.com')

    expect(await screen.findByRole('status')).toHaveTextContent('Your session has expired')
    expect(screen.getByRole('heading', { name: 'Log in' })).toBeInTheDocument()
  })

  // PT-18
  it('another user only sees their own tasks after logging in', async () => {
    const user = userEvent.setup()
    vi.stubGlobal('fetch', fakeBackend())

    render(<App />)
    await logIn(user, 'ana@example.com')
    await screen.findByText('Ana task')
    await user.click(screen.getByRole('button', { name: 'Log out' }))

    await logIn(user, 'luis@example.com')

    expect(await screen.findByText('Luis task')).toBeInTheDocument()
    expect(screen.queryByText('Ana task')).not.toBeInTheDocument()
  })

  it('registers a new account and goes back to the login form', async () => {
    const user = userEvent.setup()
    vi.stubGlobal('fetch', fakeBackend())

    render(<App />)
    await user.click(screen.getByRole('button', { name: 'Register' }))
    await user.type(screen.getByLabelText('Name'), 'Maria Lopez')
    await user.type(screen.getByLabelText('Email'), 'maria@example.com')
    await user.type(screen.getByLabelText('Password'), 'ContrasenaDemo123!')
    await user.click(screen.getByRole('button', { name: 'Create account' }))

    expect(await screen.findByRole('status')).toHaveTextContent('Account created')
  })

  it('shows the backend error when the email already exists', async () => {
    const user = userEvent.setup()
    vi.stubGlobal('fetch', fakeBackend())

    render(<App />)
    await user.click(screen.getByRole('button', { name: 'Register' }))
    await user.type(screen.getByLabelText('Name'), 'Ana Perez')
    await user.type(screen.getByLabelText('Email'), 'ana@example.com')
    await user.type(screen.getByLabelText('Password'), 'ContrasenaDemo123!')
    await user.click(screen.getByRole('button', { name: 'Create account' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Email already registered')
  })

  it('rejects a short password before calling the API', async () => {
    const user = userEvent.setup()
    const fetchMock = fakeBackend()
    vi.stubGlobal('fetch', fetchMock)

    render(<App />)
    await user.click(screen.getByRole('button', { name: 'Register' }))
    await user.type(screen.getByLabelText('Name'), 'Maria Lopez')
    await user.type(screen.getByLabelText('Email'), 'maria@example.com')
    await user.type(screen.getByLabelText('Password'), '1234567')
    await user.click(screen.getByRole('button', { name: 'Create account' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('at least 8 characters')
    await waitFor(() => expect(fetchMock).not.toHaveBeenCalled())
  })
})
