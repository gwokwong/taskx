import { api } from '@/utils/request'

export interface CalendarEvent {
  id?: number
  title: string
  description?: string
  startTime: string
  endTime: string
  allDay?: boolean
  color?: string
  location?: string
  type?: string
  status?: string
  relatedId?: number
  relatedType?: string
}

export const calendarApi = {
  createEvent(event: CalendarEvent) {
    return api.post('/calendar', event)
  },

  updateEvent(id: number, event: CalendarEvent) {
    return api.put(`/calendar/${id}`, event)
  },

  deleteEvent(id: number) {
    return api.delete(`/calendar/${id}`)
  },

  getEvent(id: number) {
    return api.get(`/calendar/${id}`)
  },

  getEventsByRange(start: string, end: string) {
    return api.get('/calendar/range', {
      params: { start, end }
    })
  },

  getUserEvents(page = 1, size = 20) {
    return api.get('/calendar', {
      params: { page, size }
    })
  },

  getTodayEvents() {
    return api.get('/calendar/today')
  },

  getUpcomingEvents(days = 7) {
    return api.get('/calendar/upcoming', {
      params: { days }
    })
  },

  getProjectEvents(projectId: number) {
    return api.get(`/calendar/project/${projectId}`)
  },

  searchEvents(keyword: string) {
    return api.get('/calendar/search', {
      params: { keyword }
    })
  },

  createTaskReminder(taskId: number, reminderTime: string) {
    return api.post('/calendar/task-reminder', null, {
      params: { taskId, reminderTime }
    })
  },

  createProjectMilestone(projectId: number, title: string, date: string) {
    return api.post('/calendar/project-milestone', null, {
      params: { projectId, title, date }
    })
  }
}

export default calendarApi