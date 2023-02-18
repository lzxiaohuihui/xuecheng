package com.xuecheng.service;

public interface IndexService {
    /**
     * @param indexName 索引名称
     * @param id 主键
     * @param object 索引对象
     * @return Boolean true 表示成功,false 失败
     * @description 添加索引
     * @author Mr.M
     * @date 2022/9/24 22:57
     */
    Boolean addCourseIndex(String indexName,String id,Object object);

}
