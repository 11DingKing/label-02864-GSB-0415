package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam_record")
public class ExamRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long userId;
    private Integer totalScore;
    private Integer status; // 0-进行中, 1-已提交, 2-已批改
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String examTitle;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String realName;
}
