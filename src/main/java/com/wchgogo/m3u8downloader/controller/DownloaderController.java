package com.wchgogo.m3u8downloader.controller;

import com.wchgogo.m3u8downloader.mapper.TaskMapper;
import com.wchgogo.m3u8downloader.po.Task;
import com.wchgogo.m3u8downloader.po.TaskExample;
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
    private TaskMapper taskMapper;

    @Resource
    private Scheduler scheduler;

    @RequestMapping("index")
    public String index(Model model) {
        return "index.html";
    }

    @ResponseBody
    @RequestMapping("taskList")
    public Result<TaskListResponse> taskList(Model model) {
        TaskExample example = new TaskExample();
        example.setOrderByClause("create_time desc");
        List<Task> taskList = taskMapper.selectByExample(example);
        TaskListResponse response = new TaskListResponse();
        response.setTaskList(taskList);
        return Result.success(response);
    }

    @ResponseBody
    @RequestMapping("addTask")
    public Result<TaskListResponse> addTask(Model model, String url, String filename) {
        try {
            Task task = new Task();
            task.setTaskId(IdUtil.nextId());
            task.setUrl(url);
            task.setFilename(filename);
            task.setFormat("mp4");
            task.setRetryTime(10);
            task.setThreadNum(10);
            task.setState(1);
            task.setCreateTime(System.currentTimeMillis());
            int rowNum = taskMapper.insert(task);
            if (rowNum <= 0) {
                return Result.error(-1, "添加任务失败");
            } else {
                scheduler.schedule(task);
                return taskList(model);
            }
        } catch (Exception e) {
            log.error("", e);
            return Result.error(-1, "添加任务失败");
        }
    }
}
