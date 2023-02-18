package com.xuecheng.dto;

import com.xuecheng.base.model.PageResult;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@ToString
public class SearchPageResultDto<T> extends PageResult {

    public SearchPageResultDto(List<T> items, long counts, long page, long
            pageSize) {
        super(items, counts, page, pageSize);
    }

}
