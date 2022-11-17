package com.twk.nccommunity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author TyanK
 * @description: file
 * @date 2022/11/11 10:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadFile {
    private int id;
    // 文件名
    private String fileName;
    // 文件大小 ~ KB
    private double fileSize;
    // 文件地址
    private String fileUrl;
    // 文件类型
    private String fileType;
    // 上传者
    private int ownerId;
    // 上传时间
    private Date uploadTime;
    // 下载次数
    private int downloadCnt;
    
}
