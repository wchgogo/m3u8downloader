package com.wchgogo.m3u8downloader.vo;

import lombok.Data;

import java.util.List;

/**
 * Author: Wang Chao
 * Date: 2020/5/2 1:35
 * Description:
 */
@Data
public class DownloadEpisodeRequest {
    private List<Episode> episodeList;
}
