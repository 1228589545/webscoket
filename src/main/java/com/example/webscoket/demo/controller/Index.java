package com.example.webscoket.demo.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by 自由翱翔峰 on 2018/12/9 15:53
 */
@Controller
public class Index {
    @RequestMapping("/")
    public String index(){
        return "index";
    }
}
