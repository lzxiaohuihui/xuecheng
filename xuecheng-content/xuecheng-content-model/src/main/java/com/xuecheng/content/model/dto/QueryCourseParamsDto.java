package com.xuecheng.model.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class QueryCourseParamsDto {

    private String auditStatus;

    private String courseName;

    private String publishStatus;
}
