package com.xuecheng.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.dto.SearchCourseParamDto;
import com.xuecheng.dto.SearchPageResultDto;
import com.xuecheng.po.CourseIndex;

public interface CourseSearchService {
    /**
     * @description 搜索课程列表
     * @param pageParams 分页参数
     * @param searchCourseParamDto 搜索条件
     * @return
    com.xuecheng.base.model.PageResult<com.xuecheng.search.po.CourseIndex> 课程列
    表
     * @author Mr.M
     * @date 2022/9/24 22:45
     */
    SearchPageResultDto<CourseIndex> queryCoursePubIndex(PageParams pageParams, SearchCourseParamDto searchCourseParamDto);

}
