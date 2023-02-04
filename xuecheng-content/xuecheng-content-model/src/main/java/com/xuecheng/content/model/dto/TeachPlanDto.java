package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class TeachPlanDto extends Teachplan {
    TeachplanMedia teachplanMedia;

    List<TeachPlanDto> teachPlanTreeNodes;
}
