package com.wchgogo.m3u8downloader.worker;

import com.wchgogo.m3u8downloader.po.Task;

/**
 * Author: Wang Chao
 * Date: 2020/4/11 21:36
 * Description:
 */
public interface IWorker {
    boolean work(Task task);
}
