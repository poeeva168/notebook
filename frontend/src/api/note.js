import request from './auth'

export const getNoteList = (params) => {
  return request.get('/notes', { params })
}

export const getNoteById = (id) => {
  return request.get(`/notes/${id}`)
}

export const createNote = (data) => {
  return request.post('/notes', data)
}

export const updateNote = (id, data) => {
  return request.put(`/notes/${id}`, data)
}

export const deleteNote = (id) => {
  return request.delete(`/notes/${id}`)
}

export const archiveNote = (id, isArchived) => {
  return request.put(`/notes/${id}/archive`, { isArchived })
}

export const restoreNote = (id) => {
  return request.put(`/notes/${id}/restore`)
}

export const deleteNotePermanently = (id) => {
  return request.delete(`/notes/${id}/permanent`)
}
