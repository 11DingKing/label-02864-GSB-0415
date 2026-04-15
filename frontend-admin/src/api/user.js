import request from './request'

export const getUserList = (params) => request.get('/user/list', { params })

export const createUser = (data) => request.post('/user', data)

export const updateUser = (id, data) => request.put(`/user/${id}`, data)

export const deleteUser = (id) => request.delete(`/user/${id}`)

export const resetPassword = (id) => request.put(`/user/${id}/reset-password`)
