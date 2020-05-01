package com.wchgogo.m3u8downloader;

import com.wchgogo.m3u8downloader.po.Task;
import com.wchgogo.m3u8downloader.service.ITaskService;
import com.wchgogo.m3u8downloader.worker.Scheduler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class M3u8downloaderApplicationTests {

    @Resource
    private Scheduler scheduler;

    @Resource
    private ITaskService taskService;

    @Test
    void contextLoads() {
        Task task = taskService.getTask(36932106005250048L);
        scheduler.work(task);
    }

}
