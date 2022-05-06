package com.twk.nccommunity.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";

    // like:entity:entityType:entityId --> set(userId)
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }
    //用户点赞数量
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }
    //某个用户关注的实体  followee:userId:entityType   zset(entityId,now)
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE +SPLIT + userId + SPLIT + entityType;
    }
    //某个实体拥有的粉丝 follower:entityType:entityId   帖子/用户 zset(userId,now)
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }
    //注入验证码至数据库
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT  + owner;
    }
    //注入登陆凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }
    //缓存用户登录信息
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }
    //单日UV访问量
    public static String getUVKey(String date){
        return PREFIX_UV + SPLIT + date;
    }
    //区间UV
    public static String getUVKey(String startDate,String endDate){
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }
    //单日访问量
    public static String getDAUKey(String date){
        return PREFIX_DAU + SPLIT + date;
    }
    //区间访问量
    public static String getDAUKey(String startDate,String endDate){
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }
    //帖子分数
    public static String getPostScoreKey(){
        return PREFIX_POST + SPLIT + "score";
    }

}
