package com.twk.nccommunity.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.DownloadUrl;
import com.twk.nccommunity.entity.Page;
import com.twk.nccommunity.entity.UploadFile;
import com.twk.nccommunity.entity.User;
import com.twk.nccommunity.service.UploadFileService;
import com.twk.nccommunity.service.UserService;
import com.twk.nccommunity.util.CommunityUtils;
import com.twk.nccommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    
    @Autowired
    private UserService userService;
    
    
    @Value(("${qiniu.bucket.file.url}"))
    private String fileBucketUrl;
    
    @RequestMapping(path = "/list",method = RequestMethod.GET)
    public String searchUploadedFile(Model model, Page page){
        page.setRows(uploadFileService.getFileRows());
        page.setPath("/files/list/");
        List<UploadFile> uploadFileList = uploadFileService.searchAllFile(page.getOffset(), page.getLimit());
        List<Map<String,Object>> uploadMap = new ArrayList<>();
        if(uploadFileList != null){
            for(UploadFile file : uploadFileList){
                HashMap<String, Object> map = new HashMap<>();
                String fileType = getFileType(file.getFileName());
                map.put("type",fileType);
                map.put("file",file);
                uploadMap.add(map);
            }
        }
        model.addAttribute("fileMap",uploadMap);
        return "site/files";
    }
    
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String addUploadFile(String UUID,String fileName, double fileSize, String fileType){
        int ownId = holder.getUsers().getId();
        fileSize = (double) Math.round((fileSize/1024) * 100)/100;
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
    
    @RequestMapping(path = "/downLoad",method = RequestMethod.POST)
    @ResponseBody
    public String addDownLoadCnt(@RequestParam("fileId") int fileId){
        if(fileId < 0){
            throw new IllegalStateException("文件ID不符合规范");
        }
        uploadFileService.addDownloadCnt(fileId);
        return CommunityUtils.getJSONString(0,"下载成功！");
    }
    
    @RequestMapping(path = "/fileInfo/{fileId}",method = RequestMethod.POST)
    @ResponseBody
    @CrossOrigin
    public String catchFileInfo(@PathVariable("fileId") int fileId) throws QiniuException {
        UploadFile file = uploadFileService.findFileById(fileId);
        String fileType = getFileType(file.getFileName());
        if(file == null){
            throw new IllegalStateException("该文件不存在!");
        }
        HashMap<String, Object> map = new HashMap<>();
        User user = userService.findUserById(file.getOwnerId());
        map.put("fileById",file);
        map.put("fileType",fileType);
        map.put("userName",user.getUsername());
        return CommunityUtils.getJSONString(0,"success",map);
    }
    
    public static String getFileType(String fileName){
        String[] split = fileName.split("\\.");
        return split[split.length-1];
    }
    
    private String getFileName(UploadFile file){
        String url = file.getFileUrl();
        String[] split = url.split("/");
        return split[split.length-1];
    }
}
