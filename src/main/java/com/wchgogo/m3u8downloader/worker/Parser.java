package com.wchgogo.m3u8downloader.worker;

import com.wchgogo.m3u8downloader.po.Task;
import com.wchgogo.m3u8downloader.service.ITaskService;
import com.wchgogo.m3u8downloader.util.Const;
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
public class Parser implements IWorker {
    @Resource
    private ITaskService taskService;

    public boolean work(Task task) {
        String url = task.getUrl();
        for (int i = 0; i < Const.PARSE_RETRY_TIMES; i++) {
            try {
                List<String> segments = new ArrayList<>();
                String data = HttpClientUtil.get(url);
                log.info("[parse] taskId={} retry={} data={}", task.getTaskId(), i, data);
                for (String item : data.replaceAll("#.*\n", "").split("\n")) {
                    item = item.trim();
                    String segmentUrl;
                    if (item.startsWith("/") || item.startsWith("http:")) {
                        segmentUrl = new URL(url).getProtocol() + "://" + new URL(url).getHost() + item;
                    } else {
                        segmentUrl = url.substring(0, url.lastIndexOf("/") + 1) + item;
                    }
                    log.info("[parse] taskId={} retry={} segmentUrl={}", task.getTaskId(), i, segmentUrl);
                    segments.add(segmentUrl);
                    break;
                }
                if (!segments.isEmpty()) {
                    taskService.parseSuccess(task, segments);
                    log.info("[parse] taskId={} retry={} success", task.getTaskId(), i);
                    return true;
                }
            } catch (Exception e) {
                log.error("[parse] taskId={} retry={} fail", task.getTaskId(), i, e);
            }
        }
        taskService.parseFail(task);
        log.info("[parse] taskId={} fail", task.getTaskId());
        return false;
    }
}
