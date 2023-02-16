package com.xuecheng;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class FreeMarkerTest {

    @Resource
    CoursePublishService coursePublishService;

    @Test
    public void testGenerateHtmlByTemplate() throws IOException, TemplateException {
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
        Template template = configuration.getTemplate("course_template.ftl");
        //指定模板文件名称
        //准备数据
        CoursePreviewDto coursePreviewInfo =
                coursePublishService.getCoursePreviewInfo(18L);
        Map<String, Object> map = new HashMap<>();
        map.put("model", coursePreviewInfo);
        //静态化
        //参数 1:模板,参数 2:数据模型
        String content =
                FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        System.out.println(content);
        //将静态化内容输出到文件中
        InputStream inputStream = IOUtils.toInputStream(content);
        //输出流
        FileOutputStream outputStream = new
                FileOutputStream("/home/lzh/test.html");
        IOUtils.copy(inputStream, outputStream);

    }
}
