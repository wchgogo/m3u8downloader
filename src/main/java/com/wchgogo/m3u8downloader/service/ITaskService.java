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
    Task addTask(Task task);

    Task getTask(Long taskId);

    List<Task> getTaskList(int pageName, int pageSize);

    void start(Task task);

    public void parseUrlFail(Task task);

    public void parseUrlSuccess(Task task);

    void parseSeqFail(Task task);

    void parseSeqSuccess(Task task, List<String> segments);

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
