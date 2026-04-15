import request from './request'

export const getQuestionsByExamId = (examId) => request.get(`/question/exam/${examId}`)

export const createQuestion = (data) => request.post('/question', data)

export const updateQuestion = (id, data) => request.put(`/question/${id}`, data)

export const deleteQuestion = (id) => request.delete(`/question/${id}`)
