package com.xuecheng.controller;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.dto.SearchCourseParamDto;
import com.xuecheng.dto.SearchPageResultDto;
import com.xuecheng.po.CourseIndex;
import com.xuecheng.service.CourseSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/course")
public class CourseSearchController {

    @Resource
    CourseSearchService courseSearchService;

    @GetMapping("/list")
    public SearchPageResultDto<CourseIndex> list(PageParams pageParams, SearchCourseParamDto searchCourseParamDto){
        return courseSearchService.queryCoursePubIndex(pageParams, searchCourseParamDto);
    }
}
