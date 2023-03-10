package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.errorprone.annotations.concurrent.UnlockMethod;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import jdk.internal.util.xml.impl.Input;
import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    @Value("${minio.bucket.files}")
    private String bucket_files;

    @Value("${minio.bucket.videofiles}")
    private String bucket_videos;

    @Autowired
    private MediaFileService currentProxy;

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //????????????????????????
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(MediaFiles::getFilename, queryMediaParamsDto.getFilename());
        //????????????
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // ??????????????????????????????
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // ??????????????????
        List<MediaFiles> list = pageResult.getRecords();
        // ??????????????????
        long total = pageResult.getTotal();
        // ???????????????
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }


    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto dto, byte[] bytes, String folder, String objectName) {
        String fileId = DigestUtils.md5Hex(bytes);

        String filename = dto.getFilename();

        if (StringUtils.isEmpty(objectName)) {
            objectName = fileId + filename.substring(filename.lastIndexOf("."));
        }

        if (StringUtils.isEmpty(folder)) {
            folder = getFileFolder(new Date(), true, true, true);
        } else if (!folder.contains("/")) {
            folder = folder + "/";
        }

        objectName = folder + objectName;
        MediaFiles mediaFiles = null;

        try {
            addMediaFilesToMinIO(bytes, bucket_files, objectName);

            mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileId, dto, bucket_files, objectName);

            UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
            BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
            return uploadFileResultDto;
        } catch (Exception e) {
            e.printStackTrace();
            XueChengException.cast("?????????????????????");
        }

        return null;
    }

    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId,
                                        String fileMd5,
                                        UploadFileParamsDto dto,
                                        String bucket,
                                        String objectName
    ) {
        String extension = null;
        if(objectName.contains(".")){
            extension = objectName.substring(objectName.lastIndexOf("."));
        }
        String contentType = ContentInfoUtil.findExtensionMatch(extension).getMimeType();
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);

        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(dto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);

            // ?????????mp4????????????url
            if (contentType.contains("image") || contentType.contains("mp4")){
                mediaFiles.setUrl("/" + bucket + "/" + objectName);
            }

            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus("002003");

            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                XueChengException.cast("????????????????????????");
            }
            //?????????avi????????????????????????
            if(contentType.equals("video/x-msvideo")){
                MediaProcess mediaProcess = new MediaProcess();
                BeanUtils.copyProperties(mediaFiles,mediaProcess);
                mediaProcess.setStatus("1");
                mediaProcessMapper.insert(mediaProcess);
            }
        }

        return mediaFiles;
    }

    public void addMediaFilesToMinIO(byte[] bytes, String bucket, String objectName) {
        //?????????????????????
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//????????????????????????

        if (objectName.indexOf(".") >= 0) {
            //???objectName???????????????
            String extension = objectName.substring(objectName.lastIndexOf("."));
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            if (extensionMatch != null) {
                contentType = extensionMatch.getMimeType();
            }

        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucket).object(objectName)
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .contentType(contentType)
                    .build();

            minioClient.putObject(putObjectArgs);

        } catch (Exception e) {
            e.printStackTrace();
            XueChengException.cast("?????????????????????????????????");
        }
    }

    //????????????????????????
    private String getFileFolder(Date date, boolean year, boolean month, boolean day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //???????????????????????????
        String dateString = sdf.format(new Date());
        //?????????????????????
        String[] dateStringArray = dateString.split("-");
        StringBuffer folderString = new StringBuffer();
        if (year) {
            folderString.append(dateStringArray[0]);
            folderString.append("/");
        }
        if (month) {
            folderString.append(dateStringArray[1]);
            folderString.append("/");
        }
        if (day) {
            folderString.append(dateStringArray[2]);
            folderString.append("/");
        }
        return folderString.toString();
    }


    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            InputStream inputStream = null;
            try {

                inputStream = minioClient.getObject(GetObjectArgs.builder()
                        .bucket(mediaFiles.getBucket())
                        .object(mediaFiles.getFilePath())
                        .build());
                if (inputStream != null) {
                    return RestResponse.success(true);
                }
            } catch (Exception ignored) {

            }
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5) + chunkIndex;

        InputStream inputStream;
        try {
            inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket_videos)
                    .object(chunkFileFolderPath)
                    .build());
            if (inputStream != null) {
                return RestResponse.success(true);
            }
        } catch (Exception ignored) {

        }

        return RestResponse.success(false);
    }

    @NotNull
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    @Override
    public RestResponse<Boolean> uploadChunk(String fileMd5, int chunk, byte[] bytes) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        String chunkFilePath = chunkFileFolderPath + chunk;

        try {
            addMediaFilesToMinIO(bytes, bucket_videos, chunkFilePath, "application/octet-stream");
        } catch (Exception e) {
            e.printStackTrace();
            XueChengException.cast("???????????????????????????");
        }
        return RestResponse.success(true);
    }

    private void addMediaFilesToMinIO(byte[] bytes, String bucket, String objectName, String s) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucket).object(objectName)
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .contentType(s)
                    .build();

            minioClient.putObject(putObjectArgs);

        } catch (Exception e) {
            e.printStackTrace();
            XueChengException.cast("?????????????????????????????????");
        }
    }

    private File[] checkChunkStatus(String fileMd5, int chunkTotal) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File[] files = new File[chunkTotal];
        for (int i = 0; i < chunkTotal; i++) {
            String chunkFilePath = chunkFileFolderPath + i;
            File chunkFile = null;
            try {
                chunkFile = File.createTempFile("chunk" + i, null);

            } catch (IOException e) {
                e.printStackTrace();
                XueChengException.cast("???????????????????????????????????????" + e.getMessage());
            }
            downloadFileFromMinIO(chunkFile, bucket_videos, chunkFilePath);
            files[i] = chunkFile;
        }
        return files;
    }

    public void downloadFileFromMinIO(File file, String bucket, String objectName) {
        InputStream fileInputStream = null;
        OutputStream fileOutputStream = null;
        try {
            fileInputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
            try {
                fileOutputStream = Files.newOutputStream(file.toPath());
                IOUtils.copy(fileInputStream, fileOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
                XueChengException.cast("????????????" + objectName + "??????");
            }
        } catch (Exception e) {
            e.printStackTrace();
            XueChengException.cast("???????????????" + objectName);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

    @Override
    public RestResponse<Boolean> mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto
            uploadFileParamsDto) {
        String filename = uploadFileParamsDto.getFilename();
        File[] chunkFiles = checkChunkStatus(fileMd5, chunkTotal);

        String extName = filename.substring(filename.lastIndexOf("."));
        File mergeFile = null;

        try {
            try {
                mergeFile = File.createTempFile(fileMd5, extName);
            } catch (IOException e) {
                XueChengException.cast("?????????????????????????????????????????????");
            }

            try {
                byte[] b = new byte[1024];
                RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
                for (File chunkFile : chunkFiles) {
                    FileInputStream chunkFileInputStream = new FileInputStream(chunkFile);
                    int len = -1;
                    while ((len = chunkFileInputStream.read(b)) != -1) {
                        raf_write.write(b, 0, len);
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
                XueChengException.cast("???????????????????????????");
            }
            log.debug("??????????????????{}", mergeFile.getAbsoluteFile());
            uploadFileParamsDto.setFileSize(mergeFile.length());

            try {
                FileInputStream mergeFileInputStream = new FileInputStream(mergeFile);
                String mergeFileMd5 = DigestUtils.md5Hex(mergeFileInputStream);
                if (!fileMd5.equalsIgnoreCase(mergeFileMd5)) {
                    XueChengException.cast("????????????????????????");
                }
                log.debug("????????????????????????{}", mergeFile.getAbsoluteFile());
            } catch (Exception e) {
                e.printStackTrace();
                XueChengException.cast("????????????????????????");
            }

            String mergeFilePathByMd5 = getFilePathByMd5(fileMd5, extName);
            try {
                addMediaFilesToMinIO(String.valueOf(mergeFile.getAbsoluteFile()), bucket_videos, mergeFilePathByMd5);
                log.debug("??????????????????MinIO??????{}", mergeFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                XueChengException.cast("?????????????????????????????????");
            }

            MediaFiles mediaFiles = addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_videos, mergeFilePathByMd5);

            if (mediaFiles == null) {
                XueChengException.cast("?????????????????????");
            }

            return RestResponse.success();
        } finally {
            for (File chunkFile : chunkFiles) {
                try {
                    chunkFile.delete();
                } catch (Exception ignored) {

                }
            }

            try {
                mergeFile.delete();
            } catch (Exception ignored) {

            }
        }


    }

    public void addMediaFilesToMinIO(String filePath, String bucket, String objectName) {
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .filename(filePath)
                            .build()
            );

        } catch (Exception e) {
            e.printStackTrace();
            XueChengException.cast("?????????????????????????????????");
        }
    }

    @Override
    public MediaFiles getFileById(String id) {
        return mediaFilesMapper.selectById(id);
    }
}
