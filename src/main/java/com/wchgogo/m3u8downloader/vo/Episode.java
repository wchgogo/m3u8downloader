package com.wchgogo.m3u8downloader.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Wang Chao
 * Date: 2020/5/2 0:12
 * Description: 单集
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Episode {
    private String name;
    private String url;
}
