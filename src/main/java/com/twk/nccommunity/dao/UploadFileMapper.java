package com.twk.nccommunity.dao;

import com.twk.nccommunity.entity.UploadFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author TyanK
 * @description: 文件管理类
 * @date 2022/11/11 10:17
 */
@Mapper
public interface UploadFileMapper {
    // 获取已上传文件列表
    List<UploadFile> selectUploadedFiles(@Param("offset") int offset,@Param("limit") int limit);
    // 获取上传文件个数
    int selectUploadedFileRows();
    // 上传文件
    int insertUploadFile(UploadFile uploadFile);
    // 下载文件 增加下载次数
    int downloadFile(@Param("id") int id);
    // 根据ID获取文件信息
    UploadFile searchFileByFileId(@Param("id") int id);
    // 侧边栏显示
    List<UploadFile> selectFilesToSideBar();
}
