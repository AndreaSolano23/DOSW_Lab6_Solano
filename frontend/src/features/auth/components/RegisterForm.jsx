import { useState } from 'react'

const MIN_PASSWORD_LENGTH = 8

export default function RegisterForm({ onSubmit, onSwitchToLogin }) {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    if (!name.trim() || !email.trim()) {
      setError('Name and email are required')
      return
    }
    if (password.length < MIN_PASSWORD_LENGTH) {
      setError(`Password must have at least ${MIN_PASSWORD_LENGTH} characters`)
      return
    }
    setError('')
    setSubmitting(true)
    try {
      await onSubmit(name.trim(), email.trim(), password)
    } catch (err) {
      // Por ejemplo 409: el correo ya existe.
      setError(err.message)
      setSubmitting(false)
    }
  }

  return (
    <form className="auth-form" onSubmit={handleSubmit}>
      <h2>Register</h2>

      <label htmlFor="register-name">Name</label>
      <input
        id="register-name"
        type="text"
        autoComplete="name"
        value={name}
        onChange={(e) => setName(e.target.value)}
      />

      <label htmlFor="register-email">Email</label>
      <input
        id="register-email"
        type="email"
        autoComplete="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
      />

      <label htmlFor="register-password">Password</label>
      <input
        id="register-password"
        type="password"
        autoComplete="new-password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
      />

      {error && <p role="alert" className="error">{error}</p>}

      <button type="submit" disabled={submitting}>
        {submitting ? 'Creating account...' : 'Create account'}
      </button>

      <p>
        Already have an account?{' '}
        <button type="button" className="link-button" onClick={onSwitchToLogin}>
          Log in
        </button>
      </p>
    </form>
  )
}
