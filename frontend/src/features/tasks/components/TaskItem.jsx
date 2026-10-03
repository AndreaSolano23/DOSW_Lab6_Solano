const STATUS_LABELS = {
  PENDING: 'Pending',
  IN_PROGRESS: 'In progress',
  COMPLETED: 'Completed',
}

const PRIORITY_LABELS = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
}

export default function TaskItem({ task, onEdit, onDelete, onChangeStatus }) {
  return (
    <li className="task-item">
      <div className="task-info">
        <strong>{task.title}</strong>
        {task.description && <p>{task.description}</p>}
        <span className="badge">{STATUS_LABELS[task.status]}</span>
        <span className="badge">{PRIORITY_LABELS[task.priority]}</span>
        <span className="due-date">{task.dueDate ?? 'No due date'}</span>
      </div>
      <div className="task-actions">
        <button type="button" aria-label={`Edit ${task.title}`} onClick={() => onEdit(task)}>
          Edit
        </button>
        <button
          type="button"
          aria-label={`Change status of ${task.title}`}
          onClick={() => onChangeStatus(task)}
        >
          Change status
        </button>
        <button type="button" aria-label={`Delete ${task.title}`} onClick={() => onDelete(task.id)}>
          Delete
        </button>
      </div>
    </li>
  )
}
