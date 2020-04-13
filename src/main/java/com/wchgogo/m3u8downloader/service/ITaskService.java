package com.wchgogo.m3u8downloader.service;

import com.wchgogo.m3u8downloader.po.Task;
import com.wchgogo.m3u8downloader.po.TaskDetail;

import java.util.List;

/**
 * Author: Wang Chao
 * Date: 2020/4/11 20:53
 * Description:
 */
public interface ITaskService {
    void start(Task task);

    void parseFail(Task task);

    void parseSuccess(Task task, List<String> segments);

    void downloadFail(Task task);

    void downloadSuccess(Task task);

    void mergeFail(Task task);

    void mergeSuccess(Task task);

    void formatFail(Task task);

    void formatSuccess(Task task);

    List<TaskDetail> getDetails(Long taskId);

    void detailDownloadFail(Task task, TaskDetail detail);

    void detailDownloadSuccess(Task task, TaskDetail detail);
}
