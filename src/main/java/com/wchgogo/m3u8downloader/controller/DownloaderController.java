package com.wchgogo.m3u8downloader.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DownloaderController {

    @RequestMapping("index")
    public ModelAndView index() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("index");
        return mv;
    }

    @RequestMapping("addTask")
    public ModelAndView addTask(String url, String filename) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("index");
        return mv;
    }
}
