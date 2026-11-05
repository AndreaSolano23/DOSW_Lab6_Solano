import { useCallback, useEffect, useState } from 'react'
import * as taskApi from '../../../api/taskApi'

export function useTasks() {
  const [tasks, setTasks] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const loadTasks = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setTasks(await taskApi.getTasks())
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  const addTask = useCallback(
    async (task) => {
      try {
        await taskApi.createTask(task)
        await loadTasks()
      } catch (err) {
        setError(err.message)
      }
    },
    [loadTasks],
  )

  const editTask = useCallback(
    async (id, task) => {
      try {
        await taskApi.updateTask(id, task)
        await loadTasks()
      } catch (err) {
        setError(err.message)
      }
    },
    [loadTasks],
  )

  const removeTask = useCallback(
    async (id) => {
      try {
        await taskApi.deleteTask(id)
        await loadTasks()
      } catch (err) {
        setError(err.message)
      }
    },
    [loadTasks],
  )

    // Carga inicial: el estado se actualiza dentro de callbacks asíncronos,
  // no de forma síncrona en el cuerpo del efecto.
  useEffect(() => {
    let active = true
    taskApi
      .getTasks()
      .then((data) => {
        if (active) {
          setTasks(data)
          setError(null)
        }
      })
      .catch((err) => {
        if (active) {
          setError(err.message)
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false)
        }
      })
    return () => {
      active = false
    }
  }, [])

  return { tasks, loading, error, loadTasks, addTask, editTask, removeTask }
}
