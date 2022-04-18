package com.twk.nccommunity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {
    @RequestMapping(path = "/user/{id}",method = RequestMethod.GET)
    @ResponseBody
    public String queryUser(@PathVariable int id){
        System.out.println(id);
        return "I'm fucking bro";
    }

    @RequestMapping(path = "/stu",method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String,Object>> getStuList(){
        List<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        map.put("name","TyanK");
        map.put("age",20);
        map.put("sex","男");
        list.add(map);
        map = new HashMap<>();
        map.put("name","hello");
        map.put("age",19);
        map.put("sex","未知");
        list.add(map);
        return list;
    }



}
