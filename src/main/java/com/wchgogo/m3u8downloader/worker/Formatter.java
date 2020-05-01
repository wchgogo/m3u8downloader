package com.wchgogo.m3u8downloader.worker;

import com.wchgogo.m3u8downloader.po.Task;
import com.wchgogo.m3u8downloader.po.TaskDetail;
import com.wchgogo.m3u8downloader.service.ITaskService;
import com.wchgogo.m3u8downloader.util.Const;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

/**
 * Author: Wang Chao
 * Date: 2020/4/12 1:46
 * Description:
 */
@Slf4j
@Order(4000)
@Service
public class Formatter implements IWorker {
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
                String destFile = Const.DOWNLOAD_DIR + "/" + task.getFilename() + "." + task.getFormat();
                String cmd = String.format("%s -y -i %s -vcodec copy -acodec copy %s",
                        Const.FFMPEG_PATH, mergeFile, destFile);
                Process process = null;
                try {
                    process = new ProcessBuilder().command(cmd.split(" ")).start();
                    int exitValue = process.waitFor();
                    if (exitValue == 0) {
                        taskService.formatSuccess(task);
                        log.info("[format] taskId={} success", task.getTaskId());
                        purge(task, mergeFile);
                        return true;
                    }
                } finally {
                    if (process != null) {
                        process.destroy();
                    }
                }
            } catch (Exception e) {
                log.error("[format] taskId={} fail", task.getTaskId(), e);
            }
        }
        taskService.formatFail(task);
        log.info("[format] taskId={} fail", task.getTaskId());
        return false;
    }

    private void purge(Task task, String path) {
        try {
            log.info("[purge] taskId={} start", task.getTaskId());
            FileUtils.forceDelete(new File(path));
            log.info("[purge] taskId={} success", task.getTaskId());
        } catch (Exception e) {
            log.info("[purge] taskId={} fail", task.getTaskId());
            log.error("[purge] taskId={} fail", task.getTaskId(), e);
        }
    }
}
