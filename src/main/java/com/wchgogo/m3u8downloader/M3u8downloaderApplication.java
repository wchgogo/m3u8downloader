package com.wchgogo.m3u8downloader;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wchgogo.m3u8downloader.mapper")
public class M3u8downloaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(M3u8downloaderApplication.class, args);
    }

}
