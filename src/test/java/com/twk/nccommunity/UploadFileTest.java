package com.twk.nccommunity;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.twk.nccommunity.entity.UploadFile;
import com.twk.nccommunity.service.UploadFileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.TimeZone;

/**
 * @author TyanK
 * @description: TODO
 * @date 2022/11/11 10:41
 */

@SpringBootTest
@ContextConfiguration(classes = NcCommunityApplication.class)
public class UploadFileTest {
    @Autowired
    private UploadFileService uploadFileService;
    
    @Test
    public void test(){
        UploadFile file = UploadFile.builder()
                .uploadTime(DateUtil.offsetHour(new Date(),+8)).build();
        uploadFileService.addUploadFile(file);
    }
    
    
    @Test
    public void testSplit(){
        String url = "http://rl226mtic.bkt.clouddn.com/6bc228905fe54d12955f56cb6a6de6ca";
        String[] split = url.split("/");
        System.out.println(split[split.length - 1]);
    }
}
