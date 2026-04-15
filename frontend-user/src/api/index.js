import request from './request'

export const login = (data) => request.post('/auth/login', data)
export const getUserInfo = () => request.get('/auth/info')
export const logout = () => request.post('/auth/logout')

export const getExamList = (params) => request.get('/exam/list', { params })
export const getExamById = (id) => request.get(`/exam/${id}`)

export const getQuestionsByExamId = (examId) => request.get(`/question/exam/${examId}`)
export const getQuestionsWithAnswer = (examId) => request.get(`/question/exam/${examId}/with-answer`)

export const startExam = (examId) => request.post(`/record/start/${examId}`)
export const submitExam = (data) => request.post('/record/submit', data)
export const getMyRecords = (params) => request.get('/record/my', { params })
export const getRecordDetail = (id) => request.get(`/record/${id}/detail`)
export const getAnswers = (id) => request.get(`/record/${id}/answers`)
