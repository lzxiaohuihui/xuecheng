package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TeachPlanController {

    @Autowired
    TeachPlanService teachPlanService;

    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachPlanDto> getTreeNodes(@PathVariable Long courseId){
        return teachPlanService.findTeachPlanTreeNodes(courseId);
    }

    @PostMapping("/teachplan")
    public void saveTeachPlan(@RequestBody SaveTeachPlanDto teachPlan){
        teachPlanService.saveTeachPlan(teachPlan);
    }
}

