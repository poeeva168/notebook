import request from './auth'

export const getAllTags = () => {
  return request.get('/tags')
}

export const createTag = (data) => {
  return request.post('/tags', data)
}

export const updateTag = (id, data) => {
  return request.put(`/tags/${id}`, data)
}

export const deleteTag = (id) => {
  return request.delete(`/tags/${id}`)
}
