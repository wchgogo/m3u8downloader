package com.wchgogo.m3u8downloader.vo;

import com.wchgogo.m3u8downloader.po.Task;
import lombok.Data;

import java.util.List;

@Data
public class TaskListResponse {
    private List<Task> taskList;
}
