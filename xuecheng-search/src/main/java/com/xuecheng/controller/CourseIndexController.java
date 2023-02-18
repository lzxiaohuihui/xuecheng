package com.xuecheng.controller;

import com.xuecheng.base.exception.XueChengException;
import com.xuecheng.po.CourseIndex;
import com.xuecheng.service.IndexService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/index")
public class CourseIndexController {

    @Value("${elasticsearch.course.index}")
    private String courseIndexStore;

    @Resource
    private IndexService indexService;

    @PostMapping("course")
    public Boolean add(@RequestBody CourseIndex courseIndex){
        Long id = courseIndex.getId();
        if (id == null){
            XueChengException.cast("课程id为空");
        }

        Boolean result = indexService.addCourseIndex(courseIndexStore, String.valueOf(id), courseIndex);

        if (!result){
            XueChengException.cast("添加课程索引失败");
        }
        return result;

    }
}
