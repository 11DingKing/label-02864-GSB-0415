package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.BusinessException;
import com.exam.dto.ExamDTO;
import com.exam.entity.Exam;
import com.exam.entity.ExamRecord;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.ExamRecordMapper;
import com.exam.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamMapper examMapper;
    private final ExamRecordMapper examRecordMapper;

    public IPage<Exam> getExamList(int page, int size, Integer status) {
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        
        if (UserContext.isStudent()) {
            // 学生只能看到已发布的考试
            wrapper.eq(Exam::getStatus, 1);
        } else {
            // 教师可以看到自己创建的所有考试
            wrapper.eq(Exam::getCreatorId, UserContext.getUserId());
            if (status != null) {
                wrapper.eq(Exam::getStatus, status);
            }
        }
        
        wrapper.orderByDesc(Exam::getCreateTime);
        return examMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public Exam getExamById(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }
        return exam;
    }

    @Transactional
    public Exam createExam(ExamDTO dto) {
        if (!UserContext.isTeacher()) {
            throw new BusinessException("只有教师才能创建考试");
        }

        Exam exam = new Exam();
        BeanUtils.copyProperties(dto, exam);
        exam.setCreatorId(UserContext.getUserId());
        exam.setStatus(0);
        examMapper.insert(exam);
        
        log.info("教师[{}]创建考试: {}", UserContext.getUserId(), exam.getTitle());
        return exam;
    }

    @Transactional
    public Exam updateExam(Long id, ExamDTO dto) {
        Exam exam = getExamById(id);
        checkExamOwner(exam);

        if (exam.getStatus() == 1) {
            throw new BusinessException("已发布的考试不能修改");
        }

        BeanUtils.copyProperties(dto, exam);
        exam.setId(id);
        examMapper.updateById(exam);
        
        log.info("教师[{}]更新考试: {}", UserContext.getUserId(), exam.getTitle());
        return exam;
    }

    @Transactional
    public void deleteExam(Long id) {
        Exam exam = getExamById(id);
        checkExamOwner(exam);

        if (exam.getStatus() == 1) {
            throw new BusinessException("已发布的考试不能删除");
        }

        examMapper.deleteById(id);
        log.info("教师[{}]删除考试: {}", UserContext.getUserId(), id);
    }

    @Transactional
    public void publishExam(Long id) {
        Exam exam = getExamById(id);
        checkExamOwner(exam);

        if (exam.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的考试才能发布");
        }

        exam.setStatus(1);
        examMapper.updateById(exam);
        log.info("教师[{}]发布考试: {}", UserContext.getUserId(), exam.getTitle());
    }

    public boolean hasUserTakenExam(Long examId, Long userId) {
        Long count = examRecordMapper.selectCount(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getUserId, userId)
        );
        return count > 0;
    }

    public boolean canTakeExam(Exam exam) {
        if (exam.getStatus() != 1) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(exam.getStartTime()) && !now.isAfter(exam.getEndTime());
    }

    private void checkExamOwner(Exam exam) {
        if (!exam.getCreatorId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权操作此考试");
        }
    }
}
