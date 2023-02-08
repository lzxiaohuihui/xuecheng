package com.xuecheng.media;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class MinIOTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    public void upload() {

        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("2.java")//同一个桶内对象名不能重复
                    .filename("/home/lzh/Documents/myJava/学成在线项目-资料/day04 媒资管理 Nacos Gateway MinIO/代码/xuecheng-plus-project148/xuecheng-plus-media/xuecheng-plus-media-service/src/test/java/com/xuecheng/media/MinIOTest.java")
                    .build();
            //上传
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传成功了");
        } catch (Exception e) {
            System.out.println("上传失败: "+ e.getMessage());
        }


    }

    @Test
    public void delete(){
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket("testbucket").object("2.java").build());
            System.out.println("删除成功");
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void bigFileTest() throws IOException{
        File sourceFile = new File("/home/lzh/Downloads/2.MP4");
        String chunkPath = "/home/lzh/Downloads/chunk/";
        File chunkFolder = new File(chunkPath);
        if (!chunkFolder.exists()){
            chunkFolder.mkdirs();
        }

        long chunkSize = 1024 * 1024 * 1;

        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        System.out.println("分块总数：" + chunkNum);

        byte[] b = new byte[1024];
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        for (long i = 0; i < chunkNum; i++) {
            File file = new File(chunkPath + i);
            if (file.exists()) {
                file.delete();
            }
            boolean newFile = file.createNewFile();
            if (newFile) {
                RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
                int len = -1;
                // 一次读一个字节
                while((len = raf_read.read(b)) != -1){
                    raf_write.write(b, 0, len);
                    if (file.length() > chunkSize) {
                        break;
                    }
                }
                raf_write.close();
            }
        }
        raf_read.close();

    }

    @Test
    public void testMerge() throws IOException{
        File chunkFolder = new File("/home/lzh/Downloads/chunk/");
        File originFile = new File("/home/lzh/Downloads/2.MP4");
        File mergeFile = new File("/home/lzh/Downloads/2_01.MP4");

        if (mergeFile.exists()){
            mergeFile.delete();
        }
        mergeFile.createNewFile();

        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");

        raf_write.seek(0);
        byte[] bytes = new byte[1024];
        File[] fileArray = chunkFolder.listFiles();

        List<File> fileList = Arrays.asList(fileArray);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });

        for (File chunkFile : fileList) {

            RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while ((len = raf_read.read(bytes)) != -1){
                raf_write.write(bytes, 0, len);
            }
            raf_read.close();
        }
        raf_write.close();

        try (
                FileInputStream originInputStream = new FileInputStream(originFile);
                FileInputStream mergeInputStream = new FileInputStream(mergeFile);
                ){
            String originMd5 = DigestUtils.md5Hex(originInputStream);
            String mergeMd5 = DigestUtils.md5Hex(mergeInputStream);

            if (originMd5.equals(mergeMd5)){
                System.out.println("合并文件成功");
            } else {
                System.out.println("合并文件失败");
            }
        }

    }
}
