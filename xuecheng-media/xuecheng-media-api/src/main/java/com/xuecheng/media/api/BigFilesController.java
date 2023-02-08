package com.xuecheng.media.api;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.service.MediaFileService;
import org.springframework.beans.Mergeable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class BigFilesController {

    @Autowired
    MediaFileService mediaFileService;

    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkFile(@RequestParam("fileMd5") String fileMd5){
        return mediaFileService.checkFile(fileMd5);
    }

    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkChunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) throws Exception{
        return mediaFileService.checkChunk(fileMd5, chunk);
    }

    @PostMapping("/upload/uploadchunk")
    public RestResponse<Boolean> uploadChunk(@RequestParam("file")MultipartFile file,
                                             @RequestParam("fileMd5") String fileMd5,
                                             @RequestParam("chunk") int chunk) throws Exception{
        return mediaFileService.uploadChunk(fileMd5, chunk, file.getBytes());
    }

    @PostMapping("upload/mergechunks")
    public RestResponse<Boolean> mergeChunks(@RequestParam("fileMd5") String fileMd5,
                                             @RequestParam("fileName") String fileName,
                                             @RequestParam("chunkTotal") int chunkTotal) throws Exception{
        Long companyId = 1232141425L;
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFileType("001002");
        uploadFileParamsDto.setFilename(fileName);
        uploadFileParamsDto.setTags("课程视频");
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(fileName);
        String mimeType = extensionMatch.getMimeType();
        uploadFileParamsDto.setContentType(mimeType);
        return mediaFileService.mergeChunks(companyId, fileMd5, chunkTotal, uploadFileParamsDto);
    }
}
