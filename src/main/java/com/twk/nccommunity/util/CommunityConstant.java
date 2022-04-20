package com.twk.nccommunity.util;

public interface CommunityConstant {
    /**
     * 激活成功---0
     * 重复激活---1
     * 激活失败---2
     */
    int ACTIVATION_SUCCESS = 0;
    int ACTIVATION_REPEAT = 1;
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认登陆凭证超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;
    /**
     * 勾选记住我用户凭证超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 30;
}
