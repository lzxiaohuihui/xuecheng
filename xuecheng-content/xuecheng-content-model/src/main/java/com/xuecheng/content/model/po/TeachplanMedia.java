package com.xuecheng.model.po;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
* 
* @TableName teachplan_media
*/
public class TeachplanMedia implements Serializable {

    /**
    * 主键
    */
    @NotNull(message="[主键]不能为空")
    @ApiModelProperty("主键")
    private Long id;
    /**
    * 媒资文件id
    */
    @Size(max= 32,message="编码长度不能超过32")
    @ApiModelProperty("媒资文件id")
    @Length(max= 32,message="编码长度不能超过32")
    private String mediaId;
    /**
    * 课程计划标识
    */
    @NotNull(message="[课程计划标识]不能为空")
    @ApiModelProperty("课程计划标识")
    private Long teachplanId;
    /**
    * 课程标识
    */
    @NotNull(message="[课程标识]不能为空")
    @ApiModelProperty("课程标识")
    private Long courseId;
    /**
    * 媒资文件原始名称
    */
    @NotBlank(message="[媒资文件原始名称]不能为空")
    @Size(max= 150,message="编码长度不能超过150")
    @ApiModelProperty("媒资文件原始名称")
    @Length(max= 150,message="编码长度不能超过150")
    private String mediaFilename;
    /**
    * 
    */
    @ApiModelProperty("")
    private Date createDate;
    /**
    * 创建人
    */
    @Size(max= 60,message="编码长度不能超过60")
    @ApiModelProperty("创建人")
    @Length(max= 60,message="编码长度不能超过60")
    private String createPeople;
    /**
    * 修改人
    */
    @Size(max= 60,message="编码长度不能超过60")
    @ApiModelProperty("修改人")
    @Length(max= 60,message="编码长度不能超过60")
    private String changePeople;

    /**
    * 主键
    */
    private void setId(Long id){
    this.id = id;
    }

    /**
    * 媒资文件id
    */
    private void setMediaId(String mediaId){
    this.mediaId = mediaId;
    }

    /**
    * 课程计划标识
    */
    private void setTeachplanId(Long teachplanId){
    this.teachplanId = teachplanId;
    }

    /**
    * 课程标识
    */
    private void setCourseId(Long courseId){
    this.courseId = courseId;
    }

    /**
    * 媒资文件原始名称
    */
    private void setMediaFilename(String mediaFilename){
    this.mediaFilename = mediaFilename;
    }

    /**
    * 
    */
    private void setCreateDate(Date createDate){
    this.createDate = createDate;
    }

    /**
    * 创建人
    */
    private void setCreatePeople(String createPeople){
    this.createPeople = createPeople;
    }

    /**
    * 修改人
    */
    private void setChangePeople(String changePeople){
    this.changePeople = changePeople;
    }


    /**
    * 主键
    */
    private Long getId(){
    return this.id;
    }

    /**
    * 媒资文件id
    */
    private String getMediaId(){
    return this.mediaId;
    }

    /**
    * 课程计划标识
    */
    private Long getTeachplanId(){
    return this.teachplanId;
    }

    /**
    * 课程标识
    */
    private Long getCourseId(){
    return this.courseId;
    }

    /**
    * 媒资文件原始名称
    */
    private String getMediaFilename(){
    return this.mediaFilename;
    }

    /**
    * 
    */
    private Date getCreateDate(){
    return this.createDate;
    }

    /**
    * 创建人
    */
    private String getCreatePeople(){
    return this.createPeople;
    }

    /**
    * 修改人
    */
    private String getChangePeople(){
    return this.changePeople;
    }

}
