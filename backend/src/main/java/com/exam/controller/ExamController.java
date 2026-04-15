package com.exam.controller;

import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.dto.ExamDTO;
import com.exam.entity.Exam;
import com.exam.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping("/list")
    public Result<PageResult<Exam>> getExamList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return Result.success(PageResult.of(examService.getExamList(page, size, status)));
    }

    @GetMapping("/{id}")
    public Result<Exam> getExamById(@PathVariable Long id) {
        return Result.success(examService.getExamById(id));
    }

    @PostMapping
    public Result<Exam> createExam(@Valid @RequestBody ExamDTO dto) {
        return Result.success(examService.createExam(dto));
    }

    @PutMapping("/{id}")
    public Result<Exam> updateExam(@PathVariable Long id, @Valid @RequestBody ExamDTO dto) {
        return Result.success(examService.updateExam(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return Result.success();
    }

    @PostMapping("/{id}/publish")
    public Result<Void> publishExam(@PathVariable Long id) {
        examService.publishExam(id);
        return Result.success();
    }
}
