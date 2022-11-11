package com.twk.nccommunity.service;

import com.twk.nccommunity.dao.UploadFileMapper;
import com.twk.nccommunity.entity.UploadFile;
import org.elasticsearch.client.license.LicensesStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author TyanK
 * @description: TODO
 * @date 2022/11/11 10:36
 */
@Service
public class UploadFileService {
    @Autowired
    private UploadFileMapper uploadFileMapper;
    
    public List<UploadFile> searchAllFile(int offset,int limit){
        return uploadFileMapper.selectUploadedFiles(offset,limit);
    } 
    
    public int getFileRows(){
        return uploadFileMapper.selectUploadedFileRows();
    }
    
    public int addUploadFile(UploadFile uploadFile){
        return uploadFileMapper.insertUploadFile(uploadFile);
    }
}
