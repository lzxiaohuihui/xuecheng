package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachPlanService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CoursePublishServiceImpl implements CoursePublishService {
    @Resource
    CourseBaseInfoService courseBaseInfoService;

    @Resource
    TeachPlanService teachPlanService;

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Resource
    CourseMarketMapper courseMarketMapper;

    @Resource
    CoursePublishPreMapper coursePublishPreMapper;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        List<TeachPlanDto> teachPlanTreeNodes = teachPlanService.findTeachPlanTreeNodes(courseId);

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachPlanTreeNodes);

        return coursePreviewDto;
    }


    @Override
    public void commitAudit(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        String auditStatus = courseBase.getAuditStatus();
        if ("202003".equals(auditStatus)) {
            XueChengException.cast("当前为等待审核状态,审核完成可以再次提交。");
        }
        if (!courseBase.getCompanyId().equals(companyId)) {
            XueChengException.cast("不允许提交其它机构的课程。");
        }
        if (StringUtils.isEmpty(courseBase.getPic())) {
            XueChengException.cast("提交失败，请上传课程图片");
        }

        CoursePublishPre coursePublishPre = new CoursePublishPre();
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String s = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(s);

        List<TeachPlanDto> teachPlanTreeNodes = teachPlanService.findTeachPlanTreeNodes(courseId);
        if (teachPlanTreeNodes.size() <= 0) {
            XueChengException.cast("提交失败，还没有添加课程计划");
        }

        String s1 = JSON.toJSONString(teachPlanTreeNodes);
        coursePublishPre.setTeachplan(s1);

        coursePublishPre.setStatus("202003");
        coursePublishPre.setCompanyId(companyId);

        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        coursePublishPre.setCreateDate(LocalDateTime.now());
        if (coursePublishPreUpdate == null) {
            coursePublishPreMapper.insert(coursePublishPre);
        }
        else{
            coursePublishPreMapper.updateById(coursePublishPreUpdate);
        }

        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }
}
