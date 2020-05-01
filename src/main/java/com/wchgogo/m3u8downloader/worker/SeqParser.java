package com.wchgogo.m3u8downloader.worker;

import com.wchgogo.m3u8downloader.po.Task;
import com.wchgogo.m3u8downloader.service.ITaskService;
import com.wchgogo.m3u8downloader.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Wang Chao
 * Date: 2020/4/11 21:19
 * Description:
 */
@Slf4j
@Order(1000)
@Service
public class SeqParser implements IWorker {
    @Resource
    private ITaskService taskService;

    public boolean work(Task task) {
        String url = task.getM3u8url();
        for (int i = 0; i < task.getRetryTime(); i++) {
            try {
                List<String> segments = new ArrayList<>();
                String data = HttpClientUtil.get(url);
                log.info("[parseSequence] taskId={} retry={} data={}", task.getTaskId(), i, data);
                for (String item : data.replaceAll("#.*\n", "").split("\n")) {
                    item = item.trim();
                    String segmentUrl;
                    if (item.startsWith("/") || item.startsWith("http:")) {
                        segmentUrl = new URL(url).getProtocol() + "://" + new URL(url).getHost() + item;
                    } else {
                        segmentUrl = url.substring(0, url.lastIndexOf("/") + 1) + item;
                    }
                    log.info("[parseSequence] taskId={} retry={} segmentUrl={}", task.getTaskId(), i, segmentUrl);
                    segments.add(segmentUrl);
                    break;
                }
                if (!segments.isEmpty()) {
                    taskService.parseSeqSuccess(task, segments);
                    log.info("[parseSequence] taskId={} retry={} success", task.getTaskId(), i);
                    return true;
                }
            } catch (Exception e) {
                log.error("[parseSequence] taskId={} retry={} fail", task.getTaskId(), i, e);
            }
        }
        taskService.parseSeqFail(task);
        log.info("[parseSequence] taskId={} fail", task.getTaskId());
        return false;
    }
}
