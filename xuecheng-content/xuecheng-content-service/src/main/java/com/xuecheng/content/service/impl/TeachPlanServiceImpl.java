package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeachPlanServiceImpl implements TeachPlanService {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Override
    public List<TeachPlanDto> findTeachPlanTreeNodes(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Transactional
    @Override
    public void saveTeachPlan(SaveTeachPlanDto dto) {
        Long id = dto.getId();

        Teachplan teachplan = teachplanMapper.selectById(id);

        if (teachplan == null){
            teachplan = new Teachplan();
            BeanUtils.copyProperties(dto, teachplan);
            int count = getTeachPlanCount(teachplan.getCourseId(), teachplan.getParentid());
            teachplan.setOrderby(count + 1);
            teachplanMapper.insert(teachplan);
        }
        else{
            BeanUtils.copyProperties(dto, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }
    private int getTeachPlanCount(long courseId, long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        return teachplanMapper.selectCount(queryWrapper);
    }
}
