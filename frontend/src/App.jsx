import './features/auth/auth.css'
import { AuthProvider } from './features/auth/context/AuthContext'
import { useAuth } from './features/auth/hooks/useAuth'
import AuthPage from './features/auth/pages/AuthPage'
import TasksPage from './features/tasks/pages/TasksPage'

function AppContent() {
  const { user, isAuthenticated, logout } = useAuth()

  if (!isAuthenticated) {
    return <AuthPage />
  }

  return (
    <>
      <header className="app-header">
        <span>{user.name}</span>
        <button type="button" onClick={logout}>
          Log out
        </button>
      </header>
      {/* key: otro usuario siempre monta una pantalla nueva, sin datos del anterior */}
      <TasksPage key={user.id} />
    </>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  )
}
