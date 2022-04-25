package com.twk.nccommunity.controller;

import com.twk.nccommunity.dao.MessageMapper;
import com.twk.nccommunity.entity.Message;
import com.twk.nccommunity.entity.Page;
import com.twk.nccommunity.entity.User;
import com.twk.nccommunity.service.MessageService;
import com.twk.nccommunity.service.UserService;
import com.twk.nccommunity.util.CommunityUtils;
import com.twk.nccommunity.util.HostHolder;
import org.omg.CORBA.OBJ_ADAPTER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Array;
import java.util.*;

@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder holder;
    @Autowired
    private UserService userService;

    /**
     * 获取所有消息
     * @param model 数据模型
     * @param page 分页
     */
    @RequestMapping(path = "/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        User user = holder.getUsers();
        page.setPath("/letter/list");
        page.setLimit(5);
        page.setRows(messageService.findConversationCount(user.getId()));
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        ArrayList<Map<String, Object>> conversations = new ArrayList<>();
        if(conversationList != null){
            for (Message message:
                conversationList) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(),message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId():message.getFromId();
                map.put("target",userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);
        model.addAttribute("letterUnreadCount",messageService.findLetterUnreadCount(user.getId(),null));
        return "site/letter";
    }

    /**
     * 获取私信内容
     * @param conversationId 私信ID
     * @param model 传入模型
     * @param page 分页
     */
    @RequestMapping(path = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Model model,Page page){
        Integer.valueOf("asdas");
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        ArrayList<Map<String, Object>> letters = new ArrayList<>();
        if(letterList != null){
            for (Message message : letterList){
                HashMap<String, Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        //更新已读消息
        List<Integer> unreadList = getUnreadList(letterList);
        if(!unreadList.isEmpty()) messageService.readMessage(unreadList);
        model.addAttribute("target",getTargetUser(conversationId));
        return "site/letter-detail";
    }


    /**
     * 发送消息接口
     * @param toName 发送给
     * @param content 内容
     * @return JSON数据表状态
     */
    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetters(String toName,String content){
        User target = userService.findUserByName(toName);
        if(target == null){
            return CommunityUtils.getJSONString(1,"发送错误,不存在该用户");
        }
        Message message = new Message();
        message.setFromId(holder.getUsers().getId());
        message.setToId(target.getId());
        if(message.getFromId() < message.getToId()){
            message.setConversationId(message.getFromId() + "_" +message.getToId());
        }else{
            message.setConversationId(message.getToId() + "_" +message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);
        return CommunityUtils.getJSONString(0);
    }


    /**
     * 筛选当前用户未读消息列表
     * @param list 原消息列表
     */
    private List<Integer> getUnreadList(List<Message> list){
        ArrayList<Integer> ids = new ArrayList<>();
        if(list != null){
            for(Message message:list){
                if(holder.getUsers().getId() == message.getToId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }


    /**
     * 判断 conversationId 前后位置
     * @param conversationId id
     */
    private User getTargetUser(String conversationId){
        String[] idx = conversationId.split("_");
        int id0 = Integer.parseInt(idx[0]);
        int id1 = Integer.parseInt(idx[1]);
        if(holder.getUsers().getId() == id0){
            return userService.findUserById(id1);
        }else{
            return userService.findUserById(id0);
        }
    }
}

