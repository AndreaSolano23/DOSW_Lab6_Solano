import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import TaskList from './TaskList'

const tasks = [
  {
    id: 1,
    title: 'Read book',
    description: 'Chapter 1',
    status: 'PENDING',
    priority: 'HIGH',
    dueDate: '2026-09-30',
  },
  {
    id: 2,
    title: 'Write report',
    description: null,
    status: 'COMPLETED',
    priority: 'LOW',
    dueDate: null,
  },
]

describe('TaskList', () => {
  it('shows the title, status, priority and due date of each task', () => {
    render(<TaskList tasks={tasks} onEdit={vi.fn()} onDelete={vi.fn()} onChangeStatus={vi.fn()} />)

    expect(screen.getByText('Read book')).toBeInTheDocument()
    expect(screen.getByText('Pending')).toBeInTheDocument()
    expect(screen.getByText('High')).toBeInTheDocument()
    expect(screen.getByText('2026-09-30')).toBeInTheDocument()
    expect(screen.getByText('Write report')).toBeInTheDocument()
    expect(screen.getByText('Completed')).toBeInTheDocument()
    expect(screen.getByText('No due date')).toBeInTheDocument()
  })

  it('calls onDelete with the task id', async () => {
    const user = userEvent.setup()
    const onDelete = vi.fn()
    render(<TaskList tasks={tasks} onEdit={vi.fn()} onDelete={onDelete} onChangeStatus={vi.fn()} />)

    await user.click(screen.getByRole('button', { name: 'Delete Read book' }))

    expect(onDelete).toHaveBeenCalledWith(1)
  })

  it('shows a message when there are no tasks', () => {
    render(<TaskList tasks={[]} onEdit={vi.fn()} onDelete={vi.fn()} onChangeStatus={vi.fn()} />)

    expect(screen.getByText('No tasks yet.')).toBeInTheDocument()
  })
})
