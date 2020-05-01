package com.wchgogo.m3u8downloader.controller;

import com.wchgogo.m3u8downloader.vo.DownloadSeasonRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class DownloaderControllerTest {

    @Resource
    private DownloaderController controller;

    @Test
    void downloadSeason() {
        DownloadSeasonRequest request = new DownloadSeasonRequest();
        request.setUrl("https://www.999meiju.com/vod/xibushijiedisanji/");
        controller.downloadSeason(request);
    }
}