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

        // 处理每道题的答案
        if (dto.getAnswers() != null) {
            for (SubmitExamDTO.AnswerDTO answerDto : dto.getAnswers()) {
                Question question = questionMap.get(answerDto.getQuestionId());
                if (question == null) continue;

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

                String userAnswer = answerDto.getAnswer() != null ? 
                        answerDto.getAnswer().toUpperCase() : "";
                
                // 对用户答案排序（处理多选题）
                String sortedUserAnswer = userAnswer.isEmpty() ? "" :
                        java.util.Arrays.stream(userAnswer.split(","))
                                .sorted()
                                .collect(Collectors.joining(","));

                boolean isCorrect = correctAnswer.equals(sortedUserAnswer);
                int score = isCorrect ? question.getScore() : 0;
                totalScore += score;

                AnswerRecord answerRecord = new AnswerRecord();
                answerRecord.setRecordId(record.getId());
                answerRecord.setQuestionId(question.getId());
                answerRecord.setAnswer(userAnswer);
                answerRecord.setIsCorrect(isCorrect ? 1 : 0);
                answerRecord.setScore(score);
                answerMapper.insert(answerRecord);
            }
        }

        record.setTotalScore(totalScore);
        record.setStatus(2); // 已批改
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
}
