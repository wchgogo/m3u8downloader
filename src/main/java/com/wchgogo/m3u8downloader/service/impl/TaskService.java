package com.wchgogo.m3u8downloader.service.impl;

import com.wchgogo.m3u8downloader.mapper.TaskDetailMapper;
import com.wchgogo.m3u8downloader.mapper.TaskMapper;
import com.wchgogo.m3u8downloader.po.Task;
import com.wchgogo.m3u8downloader.po.TaskDetail;
import com.wchgogo.m3u8downloader.po.TaskDetailExample;
import com.wchgogo.m3u8downloader.po.TaskExample;
import com.wchgogo.m3u8downloader.service.ITaskService;
import com.wchgogo.m3u8downloader.util.Const;
import com.wchgogo.m3u8downloader.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author: Wang Chao
 * Date: 2020/4/11 21:02
 * Description:
 */
@Slf4j
@Service
public class TaskService implements ITaskService {
    @Resource
    private TaskMapper taskMapper;

    @Resource
    private TaskDetailMapper taskDetailMapper;

    @Override
    public Task addTask(Task task) {
        task.setTaskId(IdUtil.nextId());
        task.setRetryTime(10);
        task.setThreadNum(10);
        task.setState(Const.STATE_NEW);
        taskMapper.insert(task);
        return task;
    }

    @Override
    public Task getTask(Long taskId) {
        return taskMapper.selectByPrimaryKey(taskId);
    }

    @Override
    public List<Task> getTaskList(int pageName, int pageSize) {
        TaskExample example = new TaskExample();
        example.setOrderByClause("create_time desc");
        return taskMapper.selectByExample(example);
    }

    @Override
    public void start(Task task) {
        task.setStartTime(System.currentTimeMillis());
        task.setElapseTime(0L);
        task.setState(Const.STATE_PARSING_URL);
        taskMapper.updateByPrimaryKey(task);
    }

    @Override
    public void parseUrlFail(Task task) {
        task.setState(Const.STATE_FAIL);
        task.setFailReason("解析url失败");
        task.setElapseTime(System.currentTimeMillis() - task.getCreateTime());
        taskMapper.updateByPrimaryKey(task);
    }

    @Override
    public void parseUrlSuccess(Task task) {
        task.setState(Const.STATE_PARSING_SEQUENCE);
        task.setElapseTime(System.currentTimeMillis() - task.getCreateTime());
        taskMapper.updateByPrimaryKey(task);
    }

    @Override
    public void parseSeqFail(Task task) {
        task.setState(Const.STATE_FAIL);
        task.setFailReason("解析失败");
        task.setElapseTime(System.currentTimeMillis() - task.getCreateTime());
        taskMapper.updateByPrimaryKey(task);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void parseSeqSuccess(Task task, List<String> segments) {
        task.setState(Const.STATE_DOWNLOADING);
        task.setTotalSegments(segments.size());
        task.setElapseTime(System.currentTimeMillis() - task.getCreateTime());
        taskMapper.updateByPrimaryKey(task);
        for (String segmentUrl : segments) {
            TaskDetail detail = new TaskDetail();
            detail.setDetailId(IdUtil.nextId());
            detail.setTaskId(task.getTaskId());
            detail.setSegmentUrl(segmentUrl);
            detail.setRetryTimes(0);
            detail.setState(Const.DETAIL_STATE_INIT);
            taskDetailMapper.insert(detail);
        }
    }

    @Override
    public void downloadFail(Task task) {
        task.setState(Const.STATE_FAIL);
        task.setFailReason("下载失败");
        task.setElapseTime(System.currentTimeMillis() - task.getCreateTime());
        taskMapper.updateByPrimaryKey(task);
    }

    @Override
    public void downloadSuccess(Task task) {
        task.setState(Const.STATE_MERGING);
        task.setElapseTime(System.currentTimeMillis() - task.getCreateTime());
        taskMapper.updateByPrimaryKey(task);
    }

    @Override
    public void mergeFail(Task task) {
        task.setState(Const.STATE_FAIL);
        task.setFailReason("合并失败");
        task.setElapseTime(System.currentTimeMillis() - task.getCreateTime());
        taskMapper.updateByPrimaryKey(task);
    }

    @Override
    public void mergeSuccess(Task task) {
        task.setState(Const.STATE_FORMATTING);
        task.setElapseTime(System.currentTimeMillis() - task.getCreateTime());
        taskMapper.updateByPrimaryKey(task);
    }

    @Override
    public void formatFail(Task task) {
        task.setState(Const.STATE_FAIL);
        task.setFailReason("转码失败");
        task.setElapseTime(System.currentTimeMillis() - task.getCreateTime());
        taskMapper.updateByPrimaryKey(task);
    }

    @Override
    public void formatSuccess(Task task) {
        task.setState(Const.STATE_SUCCESS);
        task.setElapseTime(System.currentTimeMillis() - task.getCreateTime());
        taskMapper.updateByPrimaryKey(task);
    }

    @Override
    public List<TaskDetail> getDetails(Long taskId) {
        TaskDetailExample example = new TaskDetailExample();
        example.createCriteria().andTaskIdEqualTo(taskId);
        return taskDetailMapper.selectByExample(example);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void detailDownloadFail(Task task, TaskDetail detail) {
        detail.setState(Const.DETAIL_STATE_FAIL);
        taskDetailMapper.updateByPrimaryKey(detail);
        int finishSegments = getFinishSegmentsCount(task);
        task.setFinishSegments(finishSegments);
        task.setElapseTime(System.currentTimeMillis() - task.getCreateTime());
        taskMapper.updateByPrimaryKey(task);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void detailDownloadSuccess(Task task, TaskDetail detail) {
        detail.setState(Const.DETAIL_STATE_SUCCESS);
        taskDetailMapper.updateByPrimaryKey(detail);
        int finishSegments = getFinishSegmentsCount(task);
        task.setFinishSegments(finishSegments);
        task.setElapseTime(System.currentTimeMillis() - task.getCreateTime());
        taskMapper.updateByPrimaryKey(task);
    }

    private int getFinishSegmentsCount(Task task) {
        TaskDetailExample example = new TaskDetailExample();
        example.createCriteria().andTaskIdEqualTo(task.getTaskId()).andStateGreaterThan(Const.DETAIL_STATE_INIT);
        return taskDetailMapper.countByExample(example);
    }
}
