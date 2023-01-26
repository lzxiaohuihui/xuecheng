package com.xuecheng.model.po;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
* 课程计划
* @TableName teachplan
*/
public class Teachplan implements Serializable {

    /**
    * 
    */
    @NotNull(message="[]不能为空")
    @ApiModelProperty("")
    private Long id;
    /**
    * 课程计划名称
    */
    @NotBlank(message="[课程计划名称]不能为空")
    @Size(max= 64,message="编码长度不能超过64")
    @ApiModelProperty("课程计划名称")
    @Length(max= 64,message="编码长度不能超过64")
    private String pname;
    /**
    * 课程计划父级Id
    */
    @NotNull(message="[课程计划父级Id]不能为空")
    @ApiModelProperty("课程计划父级Id")
    private Long parentid;
    /**
    * 层级，分为1、2、3级
    */
    @NotNull(message="[层级，分为1、2、3级]不能为空")
    @ApiModelProperty("层级，分为1、2、3级")
    private Integer grade;
    /**
    * 课程类型:1视频、2文档
    */
    @Size(max= 10,message="编码长度不能超过10")
    @ApiModelProperty("课程类型:1视频、2文档")
    @Length(max= 10,message="编码长度不能超过10")
    private String mediaType;
    /**
    * 开始直播时间
    */
    @ApiModelProperty("开始直播时间")
    private Date startTime;
    /**
    * 直播结束时间
    */
    @ApiModelProperty("直播结束时间")
    private Date endTime;
    /**
    * 章节及课程时介绍
    */
    @Size(max= 500,message="编码长度不能超过500")
    @ApiModelProperty("章节及课程时介绍")
    @Length(max= 500,message="编码长度不能超过500")
    private String description;
    /**
    * 时长，单位时:分:秒
    */
    @Size(max= 30,message="编码长度不能超过30")
    @ApiModelProperty("时长，单位时:分:秒")
    @Length(max= 30,message="编码长度不能超过30")
    private String timelength;
    /**
    * 排序字段
    */
    @ApiModelProperty("排序字段")
    private Integer orderby;
    /**
    * 课程标识
    */
    @NotNull(message="[课程标识]不能为空")
    @ApiModelProperty("课程标识")
    private Long courseId;
    /**
    * 课程发布标识
    */
    @ApiModelProperty("课程发布标识")
    private Long coursePubId;
    /**
    * 状态（1正常  0删除）
    */
    @NotNull(message="[状态（1正常  0删除）]不能为空")
    @ApiModelProperty("状态（1正常  0删除）")
    private Integer status;
    /**
    * 是否支持试学或预览（试看）
    */
    @ApiModelProperty("是否支持试学或预览（试看）")
    private String isPreview;
    /**
    * 创建时间
    */
    @ApiModelProperty("创建时间")
    private Date createDate;
    /**
    * 修改时间
    */
    @ApiModelProperty("修改时间")
    private Date changeDate;

    /**
    * 
    */
    private void setId(Long id){
    this.id = id;
    }

    /**
    * 课程计划名称
    */
    private void setPname(String pname){
    this.pname = pname;
    }

    /**
    * 课程计划父级Id
    */
    private void setParentid(Long parentid){
    this.parentid = parentid;
    }

    /**
    * 层级，分为1、2、3级
    */
    private void setGrade(Integer grade){
    this.grade = grade;
    }

    /**
    * 课程类型:1视频、2文档
    */
    private void setMediaType(String mediaType){
    this.mediaType = mediaType;
    }

    /**
    * 开始直播时间
    */
    private void setStartTime(Date startTime){
    this.startTime = startTime;
    }

    /**
    * 直播结束时间
    */
    private void setEndTime(Date endTime){
    this.endTime = endTime;
    }

    /**
    * 章节及课程时介绍
    */
    private void setDescription(String description){
    this.description = description;
    }

    /**
    * 时长，单位时:分:秒
    */
    private void setTimelength(String timelength){
    this.timelength = timelength;
    }

    /**
    * 排序字段
    */
    private void setOrderby(Integer orderby){
    this.orderby = orderby;
    }

    /**
    * 课程标识
    */
    private void setCourseId(Long courseId){
    this.courseId = courseId;
    }

    /**
    * 课程发布标识
    */
    private void setCoursePubId(Long coursePubId){
    this.coursePubId = coursePubId;
    }

    /**
    * 状态（1正常  0删除）
    */
    private void setStatus(Integer status){
    this.status = status;
    }

    /**
    * 是否支持试学或预览（试看）
    */
    private void setIsPreview(String isPreview){
    this.isPreview = isPreview;
    }

    /**
    * 创建时间
    */
    private void setCreateDate(Date createDate){
    this.createDate = createDate;
    }

    /**
    * 修改时间
    */
    private void setChangeDate(Date changeDate){
    this.changeDate = changeDate;
    }


    /**
    * 
    */
    private Long getId(){
    return this.id;
    }

    /**
    * 课程计划名称
    */
    private String getPname(){
    return this.pname;
    }

    /**
    * 课程计划父级Id
    */
    private Long getParentid(){
    return this.parentid;
    }

    /**
    * 层级，分为1、2、3级
    */
    private Integer getGrade(){
    return this.grade;
    }

    /**
    * 课程类型:1视频、2文档
    */
    private String getMediaType(){
    return this.mediaType;
    }

    /**
    * 开始直播时间
    */
    private Date getStartTime(){
    return this.startTime;
    }

    /**
    * 直播结束时间
    */
    private Date getEndTime(){
    return this.endTime;
    }

    /**
    * 章节及课程时介绍
    */
    private String getDescription(){
    return this.description;
    }

    /**
    * 时长，单位时:分:秒
    */
    private String getTimelength(){
    return this.timelength;
    }

    /**
    * 排序字段
    */
    private Integer getOrderby(){
    return this.orderby;
    }

    /**
    * 课程标识
    */
    private Long getCourseId(){
    return this.courseId;
    }

    /**
    * 课程发布标识
    */
    private Long getCoursePubId(){
    return this.coursePubId;
    }

    /**
    * 状态（1正常  0删除）
    */
    private Integer getStatus(){
    return this.status;
    }

    /**
    * 是否支持试学或预览（试看）
    */
    private String getIsPreview(){
    return this.isPreview;
    }

    /**
    * 创建时间
    */
    private Date getCreateDate(){
    return this.createDate;
    }

    /**
    * 修改时间
    */
    private Date getChangeDate(){
    return this.changeDate;
    }

}
