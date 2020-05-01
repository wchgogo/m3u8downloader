package com.wchgogo.m3u8downloader.worker;

import com.wchgogo.m3u8downloader.po.Task;
import com.wchgogo.m3u8downloader.service.ITaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Author: Wang Chao
 * Date: 2020/4/11 21:32
 * Description:
 */
@Slf4j
@Service
public class Scheduler {
    private static Executor executor = Executors.newSingleThreadExecutor();

    @Resource
    private ITaskService taskService;

    @Resource
    private List<IWorker> workers;

    public void schedule(Task task) {
        executor.execute(() -> {
            work(task);
        });
        log.info("[schedule] taskId={} success", task.getTaskId());
    }

    public void work(Task task) {
        log.info("[work] taskId={} start", task.getTaskId());
        taskService.start(task);
        for (IWorker worker : workers) {
            if (!worker.work(task)) {
                break;
            }
        }
        log.info("[work] taskId={} finish", task.getTaskId());
    }
}
