package com.xuecheng.content.service.jobhandler;

import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {

    public static final String MESSAGE_TYPE = "course_publish";

    @Resource
    CoursePublishService coursePublishService;

    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception{
        int stardIndex = XxlJobHelper.getShardIndex();
        int stardTotal = XxlJobHelper.getShardTotal();

        log.debug("shardIndex={}, shardTotal={}", stardIndex, stardTotal);

        process(stardIndex, stardTotal, MESSAGE_TYPE, 5, 60);
    }
    @Override
    public boolean execute(MqMessage mqMessage) {
        String businessKey1 = mqMessage.getBusinessKey1();
        long courseId = Integer.parseInt(businessKey1);
        // 课程静态化
        generateCourseHtml(mqMessage, courseId);
        // 课程缓存
        saveCourseCache(mqMessage, courseId);
        // 课程索引
        saveCourseIndex(mqMessage, courseId);

        return true;

    }

    //生成课程静态化页面并上传至文件系统
    public void generateCourseHtml(MqMessage mqMessage,long courseId){
        log.debug("开始进行课程静态化,课程 id:{}",courseId);
        //消息 id
        Long id = mqMessage.getId();
        //消息处理的 service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageOne = mqMessageService.getStageOne(id);
        if(stageOne == 1){
            log.debug("课程静态化已处理直接返回,课程 id:{}",courseId);
            return ;
        }
        //生成静态化页面
        File file = coursePublishService.generateCourseHtml(courseId);
        //上传静态化页面
        if(file!=null){
            coursePublishService.uploadCourseHtml(courseId,file);
        }
        //保存第一阶段状态
        mqMessageService.completedStageOne(id);
    }


    private void saveCourseCache(MqMessage mqMessage, long courseId) {
        log.debug("保存课程索引信息， 课程id :{}", courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }

    }

    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        log.debug("保存课程缓存至redis,课程 id:{}",courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
