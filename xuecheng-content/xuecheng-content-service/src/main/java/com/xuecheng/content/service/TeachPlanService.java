package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;

public interface TeachPlanService {

    public List<TeachPlanDto> findTeachPlanTreeNodes(long courseId);

    public void saveTeachPlan(SaveTeachPlanDto saveTeachPlanDto);

    TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);
}
