package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.injector.methods.SelectByMap;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.feignclient.model.CourseIndex;
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
import com.xuecheng.content.service.jobhandler.CoursePublishTask;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
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

    @Resource
    CoursePublishMapper coursePublishMapper;

    @Resource
    MqMessageService mqMessageService;

    @Resource
    MediaServiceClient mediaServiceClient;

    @Resource
    SearchServiceClient searchServiceClient;

    @Resource
    RedisTemplate redisTemplate;

    @Resource
    RedissonClient redissonClient;

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
            XueChengException.cast("???????????????????????????,?????????????????????????????????");
        }
//        if (!courseBase.getCompanyId().equals(companyId)) {
//            XueChengException.cast("???????????????????????????????????????");
//        }
//        if (StringUtils.isEmpty(courseBase.getPic())) {
//            XueChengException.cast("????????????????????????????????????");
//        }

        CoursePublishPre coursePublishPre = new CoursePublishPre();
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String s = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(s);

        List<TeachPlanDto> teachPlanTreeNodes = teachPlanService.findTeachPlanTreeNodes(courseId);
        if (teachPlanTreeNodes.size() <= 0) {
            XueChengException.cast("??????????????????????????????????????????");
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

    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null){
            XueChengException.cast("??????????????????????????????????????????????????????");
        }

        if (!coursePublishPre.getCompanyId().equals(companyId)){
            XueChengException.cast("????????????????????????????????????");
        }

        String status = coursePublishPre.getStatus();
        if (!"202004".equals(status)){
            XueChengException.cast("?????????????????????????????????????????????");
        }
        // ????????????????????????
        saveCoursePublish(courseId);
        // ???????????????
        saveCoursePublishMessage(courseId);

        coursePublishPreMapper.deleteById(courseId);
    }

    @Override
    public File generateCourseHtml(Long courseId) {
        //???????????????
        File htmlFile = null;
        try {
        //?????? freemarker
            Configuration configuration = new
                    Configuration(Configuration.getVersion());
            //????????????
            //?????????????????????,classpath ??? templates ???
            //?????? classpath ??????
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath +
                    "/templates/"));
            //??????????????????
            configuration.setDefaultEncoding("utf-8");
            //????????????????????????
            Template template = configuration.getTemplate("course_template.ftl");
            //????????????
            CoursePreviewDto coursePreviewInfo =
                    this.getCoursePreviewInfo(courseId);
            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);
            //?????????
            //?????? 1:??????,?????? 2:????????????
            String content =
                    FreeMarkerTemplateUtils.processTemplateIntoString (template, map);
            //
            System.out.println(content);
            //????????????????????????????????????
            InputStream inputStream = IOUtils.toInputStream(content);
            //?????????????????????
            htmlFile = File.createTempFile("course",".html");
            log.debug("???????????????,??????????????????:{}",htmlFile.getAbsolutePath());
            //?????????
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile =
                MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.upload(multipartFile, "course",
                courseId+".html");

    }

    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage(CoursePublishTask.MESSAGE_TYPE, String.valueOf(courseId), null, null);
        if (mqMessage == null){
            XueChengException.cast(CommonError.UNKOWN_ERROR);
        }
    }

    private void saveCoursePublish(Long courseId) {
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            XueChengException.cast("???????????????????????????");
        }

        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        coursePublish.setStatus("203003");
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if (coursePublishUpdate == null){
            coursePublishMapper.insert(coursePublish);
        }
        else{
            coursePublishMapper.updateById(coursePublish);
        }

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);

    }

    @Override
    public Boolean saveCourseIndex(Long courseId) {
        //????????????????????????
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        //???????????????????????????
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);
        //???????????????????????? api ???????????????????????????
        Boolean add = searchServiceClient.add(courseIndex);
        if(!add){
            XueChengException.cast("??????????????????");
        }
        return add;
    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        return coursePublishMapper.selectById(courseId);
    }

    @Override
    public CoursePublish getCoursePublishCache(Long courseId) {
        String jsonString = (String) redisTemplate.opsForValue().get("course:" + courseId);
        if (StringUtils.isNotEmpty(jsonString)){
            System.out.println("============????????????============");
            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
            return coursePublish;
        }
        else {
            RLock lock = redissonClient.getLock("coursequerylock:" + courseId);
            lock.lock();
            try {
                jsonString = (String) redisTemplate.opsForValue().get("course:" + courseId);
                if (jsonString != null){
                    System.out.println("============????????????============");
                    CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
                    return coursePublish;
                }
                System.out.println("???????????????....");
                CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish), 300, TimeUnit.SECONDS);

                return coursePublish;
            }finally {
                lock.unlock();
            }
        }
    }

}
