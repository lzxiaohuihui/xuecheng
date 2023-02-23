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
            XueChengException.cast("当前为等待审核状态,审核完成可以再次提交。");
        }
//        if (!courseBase.getCompanyId().equals(companyId)) {
//            XueChengException.cast("不允许提交其它机构的课程。");
//        }
//        if (StringUtils.isEmpty(courseBase.getPic())) {
//            XueChengException.cast("提交失败，请上传课程图片");
//        }

        CoursePublishPre coursePublishPre = new CoursePublishPre();
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String s = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(s);

        List<TeachPlanDto> teachPlanTreeNodes = teachPlanService.findTeachPlanTreeNodes(courseId);
        if (teachPlanTreeNodes.size() <= 0) {
            XueChengException.cast("提交失败，还没有添加课程计划");
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
            XueChengException.cast("请先提交课程审核，审核通过才可以发布");
        }

        if (!coursePublishPre.getCompanyId().equals(companyId)){
            XueChengException.cast("不允许提交其它机构的课程");
        }

        String status = coursePublishPre.getStatus();
        if (!"202004".equals(status)){
            XueChengException.cast("操作失败，课程审核通过才可发布");
        }
        // 保存课程发布信息
        saveCoursePublish(courseId);
        // 保存消息表
        saveCoursePublishMessage(courseId);

        coursePublishPreMapper.deleteById(courseId);
    }

    @Override
    public File generateCourseHtml(Long courseId) {
        //静态化文件
        File htmlFile = null;
        try {
        //配置 freemarker
            Configuration configuration = new
                    Configuration(Configuration.getVersion());
            //加载模板
            //选指定模板路径,classpath 下 templates 下
            //得到 classpath 路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath +
                    "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");
            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");
            //准备数据
            CoursePreviewDto coursePreviewInfo =
                    this.getCoursePreviewInfo(courseId);
            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);
            //静态化
            //参数 1:模板,参数 2:数据模型
            String content =
                    FreeMarkerTemplateUtils.processTemplateIntoString (template, map);
            //
            System.out.println(content);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile("course",".html");
            log.debug("课程静态化,生成静态文件:{}",htmlFile.getAbsolutePath());
            //输出流
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
            XueChengException.cast("课程预发布数据为空");
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
        //取出课程发布信息
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        //拷贝至课程索引对象
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);
        //远程调用搜索服务 api 添加课程信息到索引
        Boolean add = searchServiceClient.add(courseIndex);
        if(!add){
            XueChengException.cast("添加索引失败");
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
            System.out.println("============从缓存查============");
            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
            return coursePublish;
        }
        else {
            System.out.println("从数据库查....");
            CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
            redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish), 300, TimeUnit.SECONDS);

            return coursePublish;
        }
    }

}
