package com.wchgogo.m3u8downloader.worker;

import com.wchgogo.m3u8downloader.po.Task;
import com.wchgogo.m3u8downloader.po.TaskDetail;
import com.wchgogo.m3u8downloader.service.ITaskService;
import com.wchgogo.m3u8downloader.util.Const;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;

/**
 * Author: Wang Chao
 * Date: 2020/4/12 1:46
 * Description:
 */
@Slf4j
@Order(3000)
@Service
public class Merger implements IWorker {
    @Resource
    private ITaskService taskService;

    @Override
    public boolean work(Task task) {
        for (int i = 0; i < task.getRetryTime(); i++) {
            try {
                List<TaskDetail> details = taskService.getDetails(task.getTaskId());
                String segmentUrl = details.get(0).getSegmentUrl();
                String extension = segmentUrl.substring(segmentUrl.lastIndexOf("."));
                String mergeFile = Const.MERGE_DIR + "/" + task.getFilename() + extension;
                OutputStream os = new FileOutputStream(new File(mergeFile), true);
                for (TaskDetail detail : details) {
                    if (detail.getState() != Const.DETAIL_STATE_SUCCESS) {
                        log.info("[merge] taskId={} detailId={} detailState={} skip for fail",
                                task.getTaskId(), detail.getDetailId(), detail.getState());
                        continue;
                    }
                    String url = detail.getSegmentUrl();
                    String name = url.substring(url.lastIndexOf("/") + 1);
                    String path = Const.DOWNLOAD_TMP_DIR + "/" + task.getTaskId() + "/" + name;
                    File file = new File(path);
                    if (!file.exists()) {
                        log.info("[merge] taskId={} detailId={} detailState={} skip for not found",
                                task.getTaskId(), detail.getDetailId(), detail.getState());
                        continue;
                    }
                    FileInputStream is = new FileInputStream(path);
                    IOUtils.copy(is, os);
                    is.close();
                    log.info("[merge] taskId={} detailId={} detailState={} success",
                            task.getTaskId(), detail.getDetailId(), detail.getState());
                }
                os.flush();
                os.close();
                taskService.mergeSuccess(task);
                log.info("[merge] taskId={} success", task.getTaskId());
                purge(task);
                return true;
            } catch (Exception e) {
                log.error("[merge] taskId={} fail", task.getTaskId(), e);
            }
        }
        taskService.mergeFail(task);
        log.info("[merge] taskId={} fail", task.getTaskId());
        return false;
    }

    private void purge(Task task) {
        try {
            log.info("[purge] taskId={} start", task.getTaskId());
            FileUtils.deleteDirectory(new File(Const.DOWNLOAD_TMP_DIR + "/" + task.getTaskId()));
            log.info("[purge] taskId={} success", task.getTaskId());
        } catch (Exception e) {
            log.info("[purge] taskId={} fail", task.getTaskId());
            log.error("[purge] taskId={} fail", task.getTaskId(), e);
        }
    }
}
