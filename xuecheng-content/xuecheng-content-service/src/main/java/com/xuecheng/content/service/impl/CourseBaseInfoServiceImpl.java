package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired

    private CourseMarketServiceImpl courseMarketService;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        //构建查询条件对象
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //构建查询条件，根据课程名称查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName());
        //构建查询条件，根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());
        //构建查询条件，根据课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());

        //分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<CourseBase> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return courseBasePageResult;

    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {

//        校验数据库合法性
//        if(StringUtils.isBlank(dto.getName())){
//            //抛出异常
////            throw  new RuntimeException("课程名称为空");
//            XueChengException.cast("课程名称为空");
//            XueChengException.cast(CommonError.PARAMS_ERROR);
//        }
//
//        if (StringUtils.isBlank(dto.getMt())) {
//            throw new XueChengException("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getSt())) {
//            throw new XueChengException("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getGrade())) {
//            throw new XueChengException("课程等级为空");
//        }
//
//        if (StringUtils.isBlank(dto.getTeachmode())) {
//            throw new XueChengException("教育模式为空");
//        }
//
//        if (StringUtils.isBlank(dto.getUsers())) {
//            throw new XueChengException("适应人群为空");
//        }
//
//        if (StringUtils.isBlank(dto.getCharge())) {
//            throw new XueChengException("收费规则为空");
//        }


        //向Course_base课程基本信息表表添加数据
        CourseBase courseBase = new CourseBase();

//        courseBase.setName(dto.getName());
//        courseBase.setGrade(dto.getGrade());
        //以上设置数据的方法可以拷贝,从源拷贝到目标
        BeanUtils.copyProperties(dto,courseBase);
        //设置机构id
        courseBase.setCompanyId(companyId);
        //创建时间
        courseBase.setCreateDate(LocalDateTime.now());
        //审核状态默认未提交
        courseBase.setAuditStatus("202002");
        //发布状态默认为未发布
        courseBase.setStatus("203001");
        //插入成功返回1
        int insert = courseBaseMapper.insert(courseBase);
        //得到课程id
        Long courseId = courseBase.getId();

        //向数据库插入课程基本信息表，拿到课程的id
        //向课程营销表添加数据
        CourseMarket courseMarket = new CourseMarket();
        //两个对象的属性名一致，类型一样
        BeanUtils.copyProperties(dto,courseMarket);
        courseMarket.setId(courseId);
        //校验如果课程为收费，必须输入价格且大于0
//        String charge = courseMarket.getCharge();
//        if(charge.equals("201001")){
//            if(courseMarket.getPrice()==null || courseMarket.getPrice().floatValue()<=0){
////                throw new RuntimeException("课程为收费价格不能为空且必须大于0");
//                XueChengPlusException.cast("课程为收费价格不能为空且必须大于0");
//
//            }
//        }
        //向数据库插入课程营销表
        int insert1 = courseMarketMapper.insert(courseMarket);
        //插入成功返回1
//        int insert1 = courseMarketMapper.insert(courseMarket);
//        if(insert<1 && insert1<1){
        //只要有一个插入不成功抛出异常
        if(insert<1 || insert1<1){
            log.error("创建课程过程中出错:{}",dto);
            throw new RuntimeException("创建课程过程中出错");
        }

        //返回

        return getCourseBaseInfo(courseId);
    }

    public CourseBaseInfoDto getCourseBaseInfo(long courseId){
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        LambdaQueryWrapper<CourseMarket> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseMarket::getId, courseId);
        CourseMarket courseMarket = courseMarketMapper.selectOne(queryWrapper);

        if (courseBase == null || courseMarket == null) {
            return null;
        }

        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        courseBaseInfoDto.setPrice(courseMarket.getPrice());
        courseBaseInfoDto.setCharge(courseMarket.getCharge());

        return courseBaseInfoDto;
    }

    @Override
    @Transactional
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {
        Long id = dto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(id);

        if (!companyId.equals(courseBase.getCompanyId())){
            XueChengException.cast("只允许修改本机构的课程");
        }

        BeanUtils.copyProperties(dto, courseBase);

        courseBase.setChangeDate(LocalDateTime.now());
        courseBaseMapper.updateById(courseBase);

        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        if (courseMarket == null){
            courseMarket = new CourseMarket();
        }
        BeanUtils.copyProperties(dto,courseMarket);

//        String charge = dto.getCharge();
//
//        if(charge.equals("201001")){
//            BigDecimal r = dto.getPrice();
//            if (r == null || r.floatValue() <= 0){
//                XueChengException.cast("课程设置了收费价格不能为空且必须大于0");
//            }
//        }
//
//
//        boolean save = courseMarketService.saveOrUpdate(courseMarket);

        int i = saveCourseMarket(courseMarket);

        return getCourseBaseInfo(id);
    }


    private int saveCourseMarket(CourseMarket courseMarket){
        String charge = courseMarket.getCharge();
        if(StringUtils.isBlank(charge)){
            XueChengException.cast("请设置收费规则");
        }
        if(charge.equals("201001")){
            Float price = courseMarket.getPrice();
            if(price == null || price <=0){
                XueChengException.cast("课程设置了收费价格不能为空且必须大于0");
            }
        }
        boolean b = courseMarketService.saveOrUpdate(courseMarket);
        return b?1:-1;
    }

}
