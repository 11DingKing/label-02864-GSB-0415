package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.BusinessException;
import com.exam.dto.SubmitExamDTO;
import com.exam.entity.*;
import com.exam.mapper.AnswerRecordMapper;
import com.exam.mapper.ExamRecordMapper;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.QuestionOptionMapper;
import com.exam.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamRecordService {

    private final ExamRecordMapper recordMapper;
    private final AnswerRecordMapper answerMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper optionMapper;
    private final ExamService examService;

    @Transactional
    public ExamRecord startExam(Long examId) {
        if (!UserContext.isStudent()) {
            throw new BusinessException("只有学生才能参加考试");
        }

        Exam exam = examService.getExamById(examId);
        
        if (!examService.canTakeExam(exam)) {
            throw new BusinessException("考试不在有效时间内");
        }

        if (examService.hasUserTakenExam(examId, UserContext.getUserId())) {
            throw new BusinessException("您已参加过此考试");
        }

        ExamRecord record = new ExamRecord();
        record.setExamId(examId);
        record.setUserId(UserContext.getUserId());
        record.setStatus(0);
        record.setStartTime(LocalDateTime.now());
        recordMapper.insert(record);

        log.info("学生[{}]开始考试[{}]", UserContext.getUserId(), examId);
        return record;
    }

    @Transactional
    public ExamRecord submitExam(SubmitExamDTO dto) {
        ExamRecord record = recordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }

        if (!record.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权提交此考试");
        }

        if (record.getStatus() != 0) {
            throw new BusinessException("考试已提交");
        }

        // 获取所有题目
        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getExamId, record.getExamId())
        );

        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        int totalScore = 0;
        boolean hasEssayQuestion = false; // 是否有简答题

        // 处理每道题的答案
        if (dto.getAnswers() != null) {
            for (SubmitExamDTO.AnswerDTO answerDto : dto.getAnswers()) {
                Question question = questionMap.get(answerDto.getQuestionId());
                if (question == null) continue;

                String userAnswer = answerDto.getAnswer() != null ? answerDto.getAnswer() : "";
                AnswerRecord answerRecord = new AnswerRecord();
                answerRecord.setRecordId(record.getId());
                answerRecord.setQuestionId(question.getId());
                answerRecord.setAnswer(userAnswer);

                if (question.getType() == 4) { // 简答题
                    hasEssayQuestion = true;
                    // 自动评分：根据关键词匹配
                    int initialScore = calculateEssayScore(question, userAnswer);
                    answerRecord.setIsCorrect(null); // 简答题没有对错概念
                    answerRecord.setScore(initialScore);
                    totalScore += initialScore;
                } else { // 客观题（单选、多选、判断）
                    // 获取正确答案
                    List<QuestionOption> correctOptions = optionMapper.selectList(
                            new LambdaQueryWrapper<QuestionOption>()
                                    .eq(QuestionOption::getQuestionId, question.getId())
                                    .eq(QuestionOption::getIsCorrect, 1)
                    );

                    String correctAnswer = correctOptions.stream()
                            .map(QuestionOption::getOptionKey)
                            .sorted()
                            .collect(Collectors.joining(","));

                    String sortedUserAnswer = userAnswer.isEmpty() ? "" :
                            java.util.Arrays.stream(userAnswer.toUpperCase().split(","))
                                    .sorted()
                                    .collect(Collectors.joining(","));

                    boolean isCorrect = correctAnswer.equals(sortedUserAnswer);
                    int score = isCorrect ? question.getScore() : 0;
                    totalScore += score;
                    
                    answerRecord.setIsCorrect(isCorrect ? 1 : 0);
                    answerRecord.setScore(score);
                }

                answerMapper.insert(answerRecord);
            }
        }

        record.setTotalScore(totalScore);
        record.setStatus(hasEssayQuestion ? 1 : 2); // 有简答题则状态为待批改，否则已批改
        record.setSubmitTime(LocalDateTime.now());
        recordMapper.updateById(record);

        log.info("学生[{}]提交考试[{}], 得分: {}", UserContext.getUserId(), record.getExamId(), totalScore);
        return record;
    }

    public IPage<ExamRecord> getMyRecords(int page, int size) {
        return recordMapper.selectByUserId(new Page<>(page, size), UserContext.getUserId());
    }

    public IPage<ExamRecord> getExamRecords(Long examId, int page, int size) {
        Exam exam = examService.getExamById(examId);
        if (!exam.getCreatorId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权查看此考试记录");
        }
        return recordMapper.selectByExamId(new Page<>(page, size), examId);
    }

    public ExamRecord getRecordDetail(Long recordId) {
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("记录不存在");
        }

        // 学生只能查看自己的记录，教师可以查看自己考试的所有记录
        if (UserContext.isStudent() && !record.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权查看此记录");
        }

        return record;
    }

    public List<AnswerRecord> getAnswersByRecordId(Long recordId) {
        return answerMapper.selectList(
                new LambdaQueryWrapper<AnswerRecord>()
                        .eq(AnswerRecord::getRecordId, recordId)
        );
    }

    public Map<String, Object> getExamStats(Long examId) {
        Exam exam = examService.getExamById(examId);
        if (!exam.getCreatorId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权查看此考试统计");
        }
        return recordMapper.selectStatsByExamId(examId, exam.getPassScore());
    }

    /**
     * 计算简答题初始分数
     */
    private int calculateEssayScore(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return 0;
        }
        if (question.getKeywords() == null || question.getKeywords().trim().isEmpty()) {
            return 0; // 没有设置关键词时得0分，等待老师手动批改
        }
        
        String[] keywords = question.getKeywords().split(",");
        int matchCount = 0;
        String lowerUserAnswer = userAnswer.toLowerCase();
        
        for (String keyword : keywords) {
            String trimmedKeyword = keyword.trim().toLowerCase();
            if (!trimmedKeyword.isEmpty() && lowerUserAnswer.contains(trimmedKeyword)) {
                matchCount++;
            }
        }
        
        // 按命中比例计算分数
        if (keywords.length == 0) return 0;
        double ratio = (double) matchCount / keywords.length;
        return (int) Math.round(question.getScore() * ratio);
    }

    /**
     * 教师手动调整简答题分数
     */
    @Transactional
    public void updateAnswerScore(Long answerId, Integer score) {
        AnswerRecord answerRecord = answerMapper.selectById(answerId);
        if (answerRecord == null) {
            throw new BusinessException("答题记录不存在");
        }

        ExamRecord examRecord = recordMapper.selectById(answerRecord.getRecordId());
        Exam exam = examService.getExamById(examRecord.getExamId());
        if (!exam.getCreatorId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权修改此分数");
        }

        // 更新单题分数
        int oldScore = answerRecord.getScore() != null ? answerRecord.getScore() : 0;
        answerRecord.setScore(score);
        answerMapper.updateById(answerRecord);

        // 重新计算总分
        List<AnswerRecord> allAnswers = answerMapper.selectList(
                new LambdaQueryWrapper<AnswerRecord>()
                        .eq(AnswerRecord::getRecordId, answerRecord.getRecordId())
        );
        int totalScore = allAnswers.stream()
                .mapToInt(a -> a.getScore() != null ? a.getScore() : 0)
                .sum();

        examRecord.setTotalScore(totalScore);
        // 检查是否所有简答题都已批改
        boolean allGraded = allAnswers.stream()
                .noneMatch(a -> {
                    Question q = questionMapper.selectById(a.getQuestionId());
                    return q.getType() == 4 && a.getScore() == null;
                });
        examRecord.setStatus(allGraded ? 2 : 1); // 全部批改完则状态变为已批改
        recordMapper.updateById(examRecord);

        log.info("教师[{}]调整答题[{}]分数: {} -> {}", UserContext.getUserId(), answerId, oldScore, score);
    }
}
