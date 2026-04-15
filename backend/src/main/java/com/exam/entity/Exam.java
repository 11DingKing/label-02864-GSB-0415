package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam")
public class Exam {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private Integer duration;
    private Integer totalScore;
    private Integer passScore;
    private Long creatorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status; // 0-草稿, 1-已发布, 2-已结束
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
