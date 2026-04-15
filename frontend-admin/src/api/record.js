import request from './request'

export const getExamRecords = (examId, params) => request.get(`/record/exam/${examId}`, { params })

export const getExamStats = (examId) => request.get(`/record/exam/${examId}/stats`)

export const getRecordDetail = (id) => request.get(`/record/${id}/detail`)

export const getAnswers = (id) => request.get(`/record/${id}/answers`)
