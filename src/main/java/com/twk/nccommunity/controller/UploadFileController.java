package com.twk.nccommunity.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.twk.nccommunity.entity.Page;
import com.twk.nccommunity.entity.UploadFile;
import com.twk.nccommunity.service.UploadFileService;
import com.twk.nccommunity.util.CommunityUtils;
import com.twk.nccommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author TyanK
 * @description: TODO
 * @date 2022/11/11 10:30
 */
@Controller
@RequestMapping("/files")
public class UploadFileController {
    
    @Autowired
    private UploadFileService uploadFileService;
    
    @Autowired
    private HostHolder holder;

    @Value(("${qiniu.bucket.file.url}"))
    private String fileBucketUrl;
    
    @RequestMapping(path = "/list",method = RequestMethod.GET)
    public String searchUploadedFile(Model model, Page page){
        page.setRows(uploadFileService.getFileRows());
        page.setPath("/files/list/");
        List<UploadFile> uploadFileList = uploadFileService.searchAllFile(page.getOffset(), page.getLimit());
        model.addAttribute("fileList",uploadFileList);
        return null;
    }
    
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String addUploadFile(String UUID,String fileName, int fileSize, String fileType){
        int ownId = holder.getUsers().getId();
        String url = fileBucketUrl + "/" + UUID;
        UploadFile file = UploadFile.builder()
                .fileName(fileName)
                .fileSize(fileSize)
                .fileType(fileType)
                .ownerId(ownId)
                .uploadTime(DateUtil.date())
                .fileUrl(url)
                .build();
        uploadFileService.addUploadFile(file);
        return CommunityUtils.getJSONString(0);
    }
}
