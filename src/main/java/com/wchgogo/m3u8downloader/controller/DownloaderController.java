package com.wchgogo.m3u8downloader.controller;

import com.google.common.collect.Lists;
import com.wchgogo.m3u8downloader.po.Task;
import com.wchgogo.m3u8downloader.service.ITaskService;
import com.wchgogo.m3u8downloader.util.HttpClientUtil;
import com.wchgogo.m3u8downloader.vo.*;
import com.wchgogo.m3u8downloader.worker.Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Controller
public class DownloaderController {
    @Resource
    private ITaskService taskService;

    @Resource
    private Scheduler scheduler;

    @RequestMapping("index")
    public String index() {
        return "index.html";
    }

    @ResponseBody
    @RequestMapping("taskList")
    public Result<TaskListResponse> taskList() {
        List<Task> taskList = taskService.getTaskList(1, 100);
        TaskListResponse response = new TaskListResponse();
        response.setTaskList(taskList);
        return Result.success(response);
    }

    @ResponseBody
    @RequestMapping("downloadEpisodes")
    public Result<TaskListResponse> downloadEpisodes(@RequestBody DownloadEpisodeRequest request) {
        try {
            List<Episode> episodeList = request.getEpisodeList();
            if (CollectionUtils.isEmpty(episodeList)) {
                return Result.error(-1, "地址不能为空");
            }
            List<Task> taskList = Lists.newArrayList();
            for (Episode episode : episodeList) {
                Task task = new Task();
                task.setUrl(episode.getUrl());
                task.setFilename(episode.getName());
                task.setFormat("mp4");
                task.setCreateTime(System.currentTimeMillis());
                taskList.add(task);
            }

            List<Task> newTaskList = taskService.addTaskList(taskList);
            scheduler.schedule(newTaskList);
            return taskList();
        } catch (Exception e) {
            log.error("", e);
            return Result.error(-1, "添加任务失败");
        }
    }

    @ResponseBody
    @RequestMapping("downloadSeason")
    public Result<List<Episode>> downloadSeason(@RequestBody DownloadSeasonRequest request) {
        try {
            if (StringUtils.isEmpty(request.getUrl())) {
                return Result.error(-1, "地址不能为空");
            }
            String url = request.getUrl();
            String data = HttpClientUtil.get(url);
            if (StringUtils.isEmpty(data)) {
                return Result.error(-1, "未发现剧集列表");
            }
            Pattern pattern = Pattern.compile("< *?a.*?href=\"(.*?)\".*?>(.*?[0-9一二三四五六七八九第集]+.*?)< *?/ *?a *?>");
            Matcher matcher = pattern.matcher(data);
            List<Episode> episodeList = Lists.newArrayList();
            while (matcher.find()) {
                String name = matcher.group(2);
                name = name.replaceAll("<.*?>", "");
                if (StringUtils.isBlank(name)) {
                    continue;
                }
                String episodeUrl = matcher.group(1);
                if (episodeUrl.startsWith("/")) {
                    episodeUrl = new URL(url).getProtocol() + "://" + new URL(url).getHost() + episodeUrl;
                }
                episodeList.add(Episode.builder().url(episodeUrl).name(name).build());
            }
            if (CollectionUtils.isEmpty(episodeList)) {
                return Result.error(-1, "未发现剧集列表");
            }
            return Result.success(episodeList);
        } catch (Exception e) {
            log.error("", e);
            return Result.error(-1, "未发现剧集列表");
        }
    }
}
