package com.wchgogo.m3u8downloader.util;

public class Const {
    public static final int STATE_NEW = 0;
    public static final int STATE_PARSING_URL = 1;
    public static final int STATE_PARSING_SEQUENCE = 2;
    public static final int STATE_DOWNLOADING = 3;
    public static final int STATE_MERGING = 4;
    public static final int STATE_FORMATTING = 5;
    public static final int STATE_SUCCESS = 6;
    public static final int STATE_FAIL = 7;

    public static final int DETAIL_STATE_INIT = 1;
    public static final int DETAIL_STATE_SUCCESS = 2;
    public static final int DETAIL_STATE_FAIL = 3;

    public static final int DOWNLOAD_THREAD_NUM = 10;

    public static final String DOWNLOAD_TMP_DIR = "/tmp/m3u8downloader";
    public static final String MERGE_DIR = "/tmp/m3u8downloader";
    public static final String DOWNLOAD_DIR = "/sharedfolders/download";

    public static final String FFMPEG_PATH = "/usr/bin/ffmpeg";
    public static final String WEBDRIVER_CHROME_DRIVER_PATH = "/usr/bin/chromedriver.exe";

}
