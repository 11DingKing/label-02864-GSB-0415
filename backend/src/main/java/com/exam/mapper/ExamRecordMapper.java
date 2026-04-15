package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.entity.ExamRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {
    
    @Select("SELECT r.*, e.title as exam_title, u.username as user_name, u.real_name " +
            "FROM exam_record r " +
            "LEFT JOIN exam e ON r.exam_id = e.id " +
            "LEFT JOIN user u ON r.user_id = u.id " +
            "WHERE r.exam_id = #{examId} " +
            "ORDER BY r.submit_time DESC")
    IPage<ExamRecord> selectByExamId(Page<ExamRecord> page, @Param("examId") Long examId);
    
    @Select("SELECT r.*, e.title as exam_title " +
            "FROM exam_record r " +
            "LEFT JOIN exam e ON r.exam_id = e.id " +
            "WHERE r.user_id = #{userId} " +
            "ORDER BY r.create_time DESC")
    IPage<ExamRecord> selectByUserId(Page<ExamRecord> page, @Param("userId") Long userId);

    @Select("SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as submitted_count, " +
            "SUM(CASE WHEN status = 2 AND total_score >= #{passScore} THEN 1 ELSE 0 END) as pass_count, " +
            "ROUND(AVG(CASE WHEN status = 2 THEN total_score END), 1) as avg_score, " +
            "MAX(CASE WHEN status = 2 THEN total_score END) as max_score, " +
            "MIN(CASE WHEN status = 2 THEN total_score END) as min_score " +
            "FROM exam_record " +
            "WHERE exam_id = #{examId}")
    Map<String, Object> selectStatsByExamId(@Param("examId") Long examId, @Param("passScore") Integer passScore);
}
