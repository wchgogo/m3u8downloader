package com.wchgogo.m3u8downloader.controller;

import com.wchgogo.m3u8downloader.po.Task;
import com.wchgogo.m3u8downloader.service.ITaskService;
import com.wchgogo.m3u8downloader.util.IdUtil;
import com.wchgogo.m3u8downloader.vo.Result;
import com.wchgogo.m3u8downloader.vo.TaskListResponse;
import com.wchgogo.m3u8downloader.worker.Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Controller
public class DownloaderController {
    @Resource
    private ITaskService taskService;

    @Resource
    private Scheduler scheduler;

    @RequestMapping("index")
    public String index(Model model) {
        return "index.html";
    }

    @ResponseBody
    @RequestMapping("taskList")
    public Result<TaskListResponse> taskList(Model model) {
        List<Task> taskList = taskService.getTaskList(1, 100);
        TaskListResponse response = new TaskListResponse();
        response.setTaskList(taskList);
        return Result.success(response);
    }

    @ResponseBody
    @RequestMapping("addTask")
    public Result<TaskListResponse> addTask(Model model, String url, String filename) {
        try {
            Task task = new Task();
            task.setUrl(url);
            task.setFilename(filename);
            task.setFormat("mp4");
            task.setCreateTime(System.currentTimeMillis());

            Task newTask = taskService.addTask(task);
            scheduler.schedule(newTask);
            return taskList(model);
        } catch (Exception e) {
            log.error("", e);
            return Result.error(-1, "添加任务失败");
        }
    }
}
