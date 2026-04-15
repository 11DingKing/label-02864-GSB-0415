package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.common.BusinessException;
import com.exam.dto.QuestionDTO;
import com.exam.entity.Exam;
import com.exam.entity.Question;
import com.exam.entity.QuestionOption;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.QuestionOptionMapper;
import com.exam.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper optionMapper;
    private final ExamService examService;

    public List<Question> getQuestionsByExamId(Long examId, boolean includeAnswer) {
        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getExamId, examId)
                        .orderByAsc(Question::getSortOrder)
        );

        for (Question question : questions) {
            List<QuestionOption> options = optionMapper.selectList(
                    new LambdaQueryWrapper<QuestionOption>()
                            .eq(QuestionOption::getQuestionId, question.getId())
                            .orderByAsc(QuestionOption::getSortOrder)
            );
            
            if (!includeAnswer) {
                // 学生答题时不返回正确答案
                options.forEach(opt -> opt.setIsCorrect(null));
            }
            question.setOptions(options);
        }

        return questions;
    }

    @Transactional
    public Question createQuestion(QuestionDTO dto) {
        Exam exam = examService.getExamById(dto.getExamId());
        if (!exam.getCreatorId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权操作此考试");
        }

        if (exam.getStatus() == 1) {
            throw new BusinessException("已发布的考试不能添加题目");
        }

        Question question = new Question();
        question.setExamId(dto.getExamId());
        question.setType(dto.getType());
        question.setContent(dto.getContent());
        question.setScore(dto.getScore());
        question.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        question.setReferenceAnswer(dto.getReferenceAnswer());
        question.setKeywords(dto.getKeywords());
        questionMapper.insert(question);

        if (dto.getOptions() != null) {
            for (QuestionDTO.OptionDTO optDto : dto.getOptions()) {
                QuestionOption option = new QuestionOption();
                option.setQuestionId(question.getId());
                option.setOptionKey(optDto.getOptionKey());
                option.setContent(optDto.getContent());
                option.setIsCorrect(optDto.getIsCorrect());
                option.setSortOrder(optDto.getSortOrder() != null ? optDto.getSortOrder() : 0);
                optionMapper.insert(option);
            }
        }

        // 更新考试总分
        updateExamTotalScore(dto.getExamId());
        
        log.info("教师[{}]添加题目到考试[{}]", UserContext.getUserId(), dto.getExamId());
        return question;
    }

    @Transactional
    public Question updateQuestion(Long id, QuestionDTO dto) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        Exam exam = examService.getExamById(question.getExamId());
        if (!exam.getCreatorId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权操作此题目");
        }

        if (exam.getStatus() == 1) {
            throw new BusinessException("已发布的考试不能修改题目");
        }

        question.setType(dto.getType());
        question.setContent(dto.getContent());
        question.setScore(dto.getScore());
        question.setSortOrder(dto.getSortOrder());
        question.setReferenceAnswer(dto.getReferenceAnswer());
        question.setKeywords(dto.getKeywords());
        questionMapper.updateById(question);

        // 删除旧选项
        optionMapper.delete(
                new LambdaQueryWrapper<QuestionOption>()
                        .eq(QuestionOption::getQuestionId, id)
        );

        // 添加新选项
        if (dto.getOptions() != null) {
            for (QuestionDTO.OptionDTO optDto : dto.getOptions()) {
                QuestionOption option = new QuestionOption();
                option.setQuestionId(id);
                option.setOptionKey(optDto.getOptionKey());
                option.setContent(optDto.getContent());
                option.setIsCorrect(optDto.getIsCorrect());
                option.setSortOrder(optDto.getSortOrder() != null ? optDto.getSortOrder() : 0);
                optionMapper.insert(option);
            }
        }

        updateExamTotalScore(question.getExamId());
        log.info("教师[{}]更新题目[{}]", UserContext.getUserId(), id);
        return question;
    }

    @Transactional
    public void deleteQuestion(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        Exam exam = examService.getExamById(question.getExamId());
        if (!exam.getCreatorId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权操作此题目");
        }

        if (exam.getStatus() == 1) {
            throw new BusinessException("已发布的考试不能删除题目");
        }

        optionMapper.delete(
                new LambdaQueryWrapper<QuestionOption>()
                        .eq(QuestionOption::getQuestionId, id)
        );
        questionMapper.deleteById(id);

        updateExamTotalScore(question.getExamId());
        log.info("教师[{}]删除题目[{}]", UserContext.getUserId(), id);
    }

    private void updateExamTotalScore(Long examId) {
        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getExamId, examId)
        );
        int totalScore = questions.stream().mapToInt(Question::getScore).sum();
        
        Exam exam = examService.getExamById(examId);
        exam.setTotalScore(totalScore);
        // 直接更新，避免循环依赖
    }
}
