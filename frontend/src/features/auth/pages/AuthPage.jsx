import { useState } from 'react'
import LoginForm from '../components/LoginForm'
import RegisterForm from '../components/RegisterForm'
import { useAuth } from '../hooks/useAuth'

export default function AuthPage() {
  const { login, register, sessionNotice } = useAuth()
  const [mode, setMode] = useState('login')
  const [info, setInfo] = useState('')

  async function handleRegister(name, email, password) {
    await register(name, email, password)
    // Después de registrarse, el usuario inicia sesión con su cuenta nueva.
    setInfo('Account created. Please log in.')
    setMode('login')
  }

  return (
    <main className="auth-page">
      <h1>ToDo</h1>
      {mode === 'login' ? (
        <LoginForm
          onSubmit={login}
          notice={info || sessionNotice}
          onSwitchToRegister={() => {
            setInfo('')
            setMode('register')
          }}
        />
      ) : (
        <RegisterForm onSubmit={handleRegister} onSwitchToLogin={() => setMode('login')} />
      )}
    </main>
  )
}
