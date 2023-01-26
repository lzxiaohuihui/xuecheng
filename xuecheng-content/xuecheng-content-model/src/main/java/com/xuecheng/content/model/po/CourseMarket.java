package com.xuecheng.model.po;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
* 课程营销信息
* @TableName course_market
*/
public class CourseMarket implements Serializable {

    /**
    * 主键，课程id
    */
    @NotNull(message="[主键，课程id]不能为空")
    @ApiModelProperty("主键，课程id")
    private Long id;
    /**
    * 收费规则，对应数据字典
    */
    @NotBlank(message="[收费规则，对应数据字典]不能为空")
    @Size(max= 32,message="编码长度不能超过32")
    @ApiModelProperty("收费规则，对应数据字典")
    @Length(max= 32,message="编码长度不能超过32")
    private String charge;
    /**
    * 现价
    */
    @ApiModelProperty("现价")
    private Double price;
    /**
    * 原价
    */
    @ApiModelProperty("原价")
    private Double originalPrice;
    /**
    * 咨询qq
    */
    @Size(max= 32,message="编码长度不能超过32")
    @ApiModelProperty("咨询qq")
    @Length(max= 32,message="编码长度不能超过32")
    private String qq;
    /**
    * 微信
    */
    @Size(max= 64,message="编码长度不能超过64")
    @ApiModelProperty("微信")
    @Length(max= 64,message="编码长度不能超过64")
    private String wechat;
    /**
    * 电话
    */
    @Size(max= 32,message="编码长度不能超过32")
    @ApiModelProperty("电话")
    @Length(max= 32,message="编码长度不能超过32")
    private String phone;
    /**
    * 有效期天数
    */
    @ApiModelProperty("有效期天数")
    private Integer validDays;

    /**
    * 主键，课程id
    */
    private void setId(Long id){
    this.id = id;
    }

    /**
    * 收费规则，对应数据字典
    */
    private void setCharge(String charge){
    this.charge = charge;
    }

    /**
    * 现价
    */
    private void setPrice(Double price){
    this.price = price;
    }

    /**
    * 原价
    */
    private void setOriginalPrice(Double originalPrice){
    this.originalPrice = originalPrice;
    }

    /**
    * 咨询qq
    */
    private void setQq(String qq){
    this.qq = qq;
    }

    /**
    * 微信
    */
    private void setWechat(String wechat){
    this.wechat = wechat;
    }

    /**
    * 电话
    */
    private void setPhone(String phone){
    this.phone = phone;
    }

    /**
    * 有效期天数
    */
    private void setValidDays(Integer validDays){
    this.validDays = validDays;
    }


    /**
    * 主键，课程id
    */
    private Long getId(){
    return this.id;
    }

    /**
    * 收费规则，对应数据字典
    */
    private String getCharge(){
    return this.charge;
    }

    /**
    * 现价
    */
    private Double getPrice(){
    return this.price;
    }

    /**
    * 原价
    */
    private Double getOriginalPrice(){
    return this.originalPrice;
    }

    /**
    * 咨询qq
    */
    private String getQq(){
    return this.qq;
    }

    /**
    * 微信
    */
    private String getWechat(){
    return this.wechat;
    }

    /**
    * 电话
    */
    private String getPhone(){
    return this.phone;
    }

    /**
    * 有效期天数
    */
    private Integer getValidDays(){
    return this.validDays;
    }

}
