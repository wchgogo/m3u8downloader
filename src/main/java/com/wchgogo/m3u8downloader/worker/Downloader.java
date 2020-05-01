package com.wchgogo.m3u8downloader.worker;

import com.wchgogo.m3u8downloader.po.Task;
import com.wchgogo.m3u8downloader.po.TaskDetail;
import com.wchgogo.m3u8downloader.service.ITaskService;
import com.wchgogo.m3u8downloader.util.Const;
import com.wchgogo.m3u8downloader.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Author: Wang Chao
 * Date: 2020/4/12 0:30
 * Description:
 */
@Slf4j
@Order(2000)
@Service
public class Downloader implements IWorker {
    @Resource
    private ITaskService taskService;

    @Override
    public boolean work(Task task) {
        List<TaskDetail> details = taskService.getDetails(task.getTaskId());
        LinkedBlockingQueue<TaskDetail> queue = new LinkedBlockingQueue<>(details);
        try {
            final CountDownLatch latch = new CountDownLatch(Const.DOWNLOAD_THREAD_NUM);
            for (int i = 0; i < Const.DOWNLOAD_THREAD_NUM; i++) {
                new Thread(() -> {
                    while (true) {
                        TaskDetail detail = queue.poll();
                        if (detail == null) {
                            latch.countDown();
                            break;
                        }
                        String url = detail.getSegmentUrl();
                        try {
                            if (detail.getRetryTimes() > task.getRetryTime()) {
                                taskService.detailDownloadFail(task, detail);
                                log.info("[download] taskId={} detailId={} retry={} url={} fail",
                                        task.getTaskId(), detail.getDetailId(), detail.getRetryTimes(), url);
                                continue;
                            }
                            String name = url.substring(url.lastIndexOf("/") + 1);
                            String path = Const.DOWNLOAD_TMP_DIR + "/" + task.getTaskId() + "/" + name;
                            HttpClientUtil.downloadFile(url, path);
                            taskService.detailDownloadSuccess(task, detail);
                            log.info("[download] taskId={} detailId={} retry={} url={} success",
                                    task.getTaskId(), detail.getDetailId(), detail.getRetryTimes(), url);
                        } catch (Exception e) {
                            log.error("[download] taskId={} detailId={} retry={} url={}",
                                    task.getTaskId(), detail.getDetailId(), detail.getRetryTimes(), url, e);
                            detail.setRetryTimes(detail.getRetryTimes() + 1);
                            queue.add(detail);
                        }
                    }
                }).start();
            }
            latch.await();
            taskService.downloadSuccess(task);
            return true;
        } catch (Exception e) {
            log.error("[download] taskId={}", task.getTaskId(), e);
        }
        taskService.downloadFail(task);
        return false;
    }
}
