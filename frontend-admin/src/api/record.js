import request from './request'

export const getExamRecords = (examId, params) => request.get(`/record/exam/${examId}`, { params })

export const getExamStats = (examId) => request.get(`/record/exam/${examId}/stats`)

export const getRecordDetail = (id) => request.get(`/record/${id}/detail`)

export const getAnswers = (id) => request.get(`/record/${id}/answers`)

export const getRecordAnswers = (id) => request.get(`/record/${id}/answers`)

export const updateAnswerScore = (answerId, score) => request.put(`/record/answer/${answerId}/score?score=${score}`)
