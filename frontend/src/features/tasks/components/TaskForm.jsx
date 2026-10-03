import { useState } from 'react'

export default function TaskForm({ initialTask = null, onSubmit, onCancel }) {
  const isEditing = initialTask !== null
  const [title, setTitle] = useState(initialTask?.title ?? '')
  const [description, setDescription] = useState(initialTask?.description ?? '')
  const [status, setStatus] = useState(initialTask?.status ?? 'PENDING')
  const [priority, setPriority] = useState(initialTask?.priority ?? 'MEDIUM')
  const [dueDate, setDueDate] = useState(initialTask?.dueDate ?? '')
  const [validationError, setValidationError] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()
    if (!title.trim()) {
      setValidationError('Title is required')
      return
    }
    setValidationError('')

    const payload = isEditing
      ? { title: title.trim(), description, status, priority, dueDate: dueDate || null }
      : { title: title.trim(), description, priority, dueDate: dueDate || null }

    await onSubmit(payload)

    if (!isEditing) {
      setTitle('')
      setDescription('')
      setPriority('MEDIUM')
      setDueDate('')
    }
  }

  return (
    <form className="task-form" onSubmit={handleSubmit}>
      <h2>{isEditing ? 'Edit task' : 'New task'}</h2>

      <label htmlFor="task-title">Title</label>
      <input
        id="task-title"
        type="text"
        value={title}
        maxLength={120}
        onChange={(event) => setTitle(event.target.value)}
      />
      {validationError && <p role="alert" className="error">{validationError}</p>}

      <label htmlFor="task-description">Description</label>
      <textarea
        id="task-description"
        value={description}
        maxLength={500}
        onChange={(event) => setDescription(event.target.value)}
      />

      {isEditing && (
        <>
          <label htmlFor="task-status">Status</label>
          <select
            id="task-status"
            value={status}
            onChange={(event) => setStatus(event.target.value)}
          >
            <option value="PENDING">Pending</option>
            <option value="IN_PROGRESS">In progress</option>
            <option value="COMPLETED">Completed</option>
          </select>
        </>
      )}

      <label htmlFor="task-priority">Priority</label>
      <select
        id="task-priority"
        value={priority}
        onChange={(event) => setPriority(event.target.value)}
      >
        <option value="LOW">Low</option>
        <option value="MEDIUM">Medium</option>
        <option value="HIGH">High</option>
      </select>

      <label htmlFor="task-due-date">Due date</label>
      <input
        id="task-due-date"
        type="date"
        value={dueDate}
        onChange={(event) => setDueDate(event.target.value)}
      />

      <div className="form-actions">
        <button type="submit">Save</button>
        {isEditing && (
          <button type="button" onClick={onCancel}>Cancel</button>
        )}
      </div>
    </form>
  )
}
