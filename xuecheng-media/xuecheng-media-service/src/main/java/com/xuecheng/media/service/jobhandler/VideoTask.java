package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class VideoTask {

    @Resource
    private MediaFileService mediaFileService;

    @Resource
    private MediaFileProcessService mediaFileProcessService;

    @Value("${videoprocess.ffmpegpath}")
    String ffmpegPath;


    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex=" + shardIndex + ", shardTotal=" + shardTotal);
        // 一次取出2条记录

        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, 2);


        //数据个数
        int size = mediaProcessList.size();
        log.debug("取出待处理视频记录{}条", size);
        if (size <= 0) return;

        //启动线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(size);

        CountDownLatch countDownLatch = new CountDownLatch(size);

        for (MediaProcess mediaProcess : mediaProcessList) {
            threadPool.execute(() -> {
                // 桶
                String bucket = mediaProcess.getBucket();
                // 存储路经
                String filePath = mediaProcess.getFilePath();

                String fileId = mediaProcess.getFileId();
                String filename = mediaProcess.getFilename();

                // 下载文件
                File originalVideo = null;
                File mp4Video = null;
                try {
                    originalVideo = File.createTempFile("original", null);
                    mp4Video = File.createTempFile("mp4", ".mp4");
                } catch (IOException e) {
                    countDownLatch.countDown();
                    log.error("下载待处理的原始文件前创建临时文件失败");
                }

                try {
                    // 下载文件
                    mediaFileService.downloadFileFromMinIO(originalVideo, bucket, filePath);

                    // 开始处理视频
                    Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegPath, originalVideo.getAbsolutePath(), mp4Video.getName(), mp4Video.getAbsolutePath());
                    String result = mp4VideoUtil.generateMp4();
                    if (!result.equals("success")) {
                        log.error("generateMp4 error ,video_path is {},error msg is {}", bucket + filePath, result);
                    }
                    mediaFileProcessService.saveProcessFinishStatus(3, fileId, null, result);
                    //将mp4上传至minio
                    //文件路径
                    String objectName = getFilePath(fileId, ".mp4");
                    mediaFileService.addMediaFilesToMinIO(mp4Video.getAbsolutePath(), bucket, objectName);
                    String url = "/" + bucket + "/" + objectName;
                    mediaFileProcessService.saveProcessFinishStatus(2, fileId, url, null);

                } catch (Exception e) {
                    countDownLatch.countDown();
                    return;
                } finally {
                    //清理文件
                    if (originalVideo != null) {
                        try {
                            originalVideo.delete();
                        } catch (Exception e) {

                        }
                    }
                    if (mp4Video != null) {
                        try {
                            mp4Video.delete();
                        } catch (Exception e) {

                        }
                    }
                }
                countDownLatch.countDown();
            });
        }

        countDownLatch.await(30, TimeUnit.MINUTES);

    }

    private String getFilePath(String fileMd5, String fileExt) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }
}
