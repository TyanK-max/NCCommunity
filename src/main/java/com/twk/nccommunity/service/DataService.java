package com.twk.nccommunity.service;

import com.twk.nccommunity.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * 统计活跃用户数据
 */
@Service
public class DataService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    //将指定ip计入UV(独立访客)
    public void recordUV(String ip){
        String uvKey = RedisKeyUtil.getUVKey(df.format(new Date()).substring(0,10));
        redisTemplate.opsForHyperLogLog().add(uvKey,ip);
    }
    //统计指定日期范围内的UV
    public long calculateUV(Date start,Date end){
        if(start == null || end == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String redisKey = RedisKeyUtil.getUVKey(df.format(calendar.getTime()).substring(0,10));
            keyList.add(redisKey);
            calendar.add(Calendar.DATE,1);
        }
        //合并数据
        String uvKey = RedisKeyUtil.getUVKey(df.format(start).substring(0,10), df.format(end).substring(0,10));
        redisTemplate.opsForHyperLogLog().union(uvKey,keyList.toArray());
        Long size = redisTemplate.opsForHyperLogLog().size(uvKey);
        return size;
    }
    //将指定用户加入日活跃用户
    public void recordDAU(int userId){
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()).substring(0,10));
        redisTemplate.opsForValue().setBit(redisKey,userId,true);
    }
    //统计范围内的DAU
    public long calculateDAU(Date start,Date end){
        if(start == null || end == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String redisKey = RedisKeyUtil.getUVKey(df.format(calendar.getTime()).substring(0,10));
            keyList.add(redisKey.getBytes());
            calendar.add(Calendar.DATE,1);
        }
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start).substring(0,10), df.format(end).substring(0,10));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(),keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }
}
