package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author Mr.M
     * @date 2022/9/10 8:57
     */
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto dto, byte[] bytes, String folder, String objectName);
    public MediaFiles addMediaFilesToDb(Long companyId,
                                        String fileMd5,
                                        UploadFileParamsDto dto,
                                        String bucket,
                                        String objectName
    );
    public void addMediaFilesToMinIO(byte[] bytes, String bucket, String objectName);
    public void addMediaFilesToMinIO(String filePath, String bucket, String objectName);

    public RestResponse<Boolean> checkFile(String fileMd5);
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);
    public RestResponse<Boolean> uploadChunk(String fileMd5,int chunk,byte[] bytes);
    public RestResponse<Boolean> mergeChunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);

    public MediaFiles getFileById(String id);

    void downloadFileFromMinIO(File originalVideo, String bucket, String filePath);
}
