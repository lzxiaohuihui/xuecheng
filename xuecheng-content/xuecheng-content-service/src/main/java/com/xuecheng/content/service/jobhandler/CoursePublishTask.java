package com.xuecheng.content.service.jobhandler;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {

    public static final String MESSAGE_TYPE = "course_publish";

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

    private void generateCourseHtml(MqMessage mqMessage, long courseId) {
      log.debug("开始进行课程静态化， 课程id： {}", courseId);
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(id);
        if (stageOne == 1){
            log.debug("课程静态化已处理直接返回， 课程id: {}", courseId);
            return;
        }
        try {
            TimeUnit.SECONDS.sleep(10);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }

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
