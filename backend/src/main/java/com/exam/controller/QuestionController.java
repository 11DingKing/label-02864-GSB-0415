package com.exam.controller;

import com.exam.common.Result;
import com.exam.dto.QuestionDTO;
import com.exam.entity.Question;
import com.exam.service.QuestionService;
import com.exam.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/exam/{examId}")
    public Result<List<Question>> getQuestionsByExamId(@PathVariable Long examId) {
        // 教师可以看到答案，学生不能
        boolean includeAnswer = UserContext.isTeacher();
        return Result.success(questionService.getQuestionsByExamId(examId, includeAnswer));
    }

    @GetMapping("/exam/{examId}/with-answer")
    public Result<List<Question>> getQuestionsWithAnswer(@PathVariable Long examId) {
        // 用于查看答卷详情时显示正确答案
        return Result.success(questionService.getQuestionsByExamId(examId, true));
    }

    @PostMapping
    public Result<Question> createQuestion(@Valid @RequestBody QuestionDTO dto) {
        return Result.success(questionService.createQuestion(dto));
    }

    @PutMapping("/{id}")
    public Result<Question> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionDTO dto) {
        return Result.success(questionService.updateQuestion(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success();
    }
}
