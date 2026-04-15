package com.exam.controller;

import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.dto.SubmitExamDTO;
import com.exam.entity.AnswerRecord;
import com.exam.entity.ExamRecord;
import com.exam.service.ExamRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/record")
@RequiredArgsConstructor
public class ExamRecordController {

    private final ExamRecordService recordService;

    @PostMapping("/start/{examId}")
    public Result<ExamRecord> startExam(@PathVariable Long examId) {
        return Result.success(recordService.startExam(examId));
    }

    @PostMapping("/submit")
    public Result<ExamRecord> submitExam(@Valid @RequestBody SubmitExamDTO dto) {
        return Result.success(recordService.submitExam(dto));
    }

    @GetMapping("/my")
    public Result<PageResult<ExamRecord>> getMyRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(PageResult.of(recordService.getMyRecords(page, size)));
    }

    @GetMapping("/exam/{examId}")
    public Result<PageResult<ExamRecord>> getExamRecords(
            @PathVariable Long examId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(PageResult.of(recordService.getExamRecords(examId, page, size)));
    }

    @GetMapping("/{id}/detail")
    public Result<ExamRecord> getRecordDetail(@PathVariable Long id) {
        return Result.success(recordService.getRecordDetail(id));
    }

    @GetMapping("/{id}/answers")
    public Result<List<AnswerRecord>> getAnswers(@PathVariable Long id) {
        return Result.success(recordService.getAnswersByRecordId(id));
    }

    @GetMapping("/exam/{examId}/stats")
    public Result<Map<String, Object>> getExamStats(@PathVariable Long examId) {
        return Result.success(recordService.getExamStats(examId));
    }
}
