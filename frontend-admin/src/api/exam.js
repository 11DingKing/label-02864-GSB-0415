import request from './request'

export const getExamList = (params) => request.get('/exam/list', { params })

export const getExamById = (id) => request.get(`/exam/${id}`)

export const createExam = (data) => request.post('/exam', data)

export const updateExam = (id, data) => request.put(`/exam/${id}`, data)

export const deleteExam = (id) => request.delete(`/exam/${id}`)

export const publishExam = (id) => request.post(`/exam/${id}/publish`)
