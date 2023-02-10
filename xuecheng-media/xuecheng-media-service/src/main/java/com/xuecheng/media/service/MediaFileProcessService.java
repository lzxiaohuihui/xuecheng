package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

public interface MediaFileProcessService {

    void saveProcessFinishStatus(int status, String fileId, String url, String errorMsg);

    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);



}
