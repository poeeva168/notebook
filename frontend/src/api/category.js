import request from './auth'

export const getCategoryTree = () => {
  return request.get('/categories')
}

export const createCategory = (data) => {
  return request.post('/categories', data)
}

export const updateCategory = (id, data) => {
  return request.put(`/categories/${id}`, data)
}

export const deleteCategory = (id) => {
  return request.delete(`/categories/${id}`)
}
