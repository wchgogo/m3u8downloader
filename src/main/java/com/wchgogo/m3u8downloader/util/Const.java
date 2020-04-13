package com.wchgogo.m3u8downloader.util;

public class Const {
    public static final int STATE_PARSING = 1;
    public static final int STATE_DOWNLOADING = 2;
    public static final int STATE_MERGING = 3;
    public static final int STATE_FORMATTING = 4;
    public static final int STATE_SUCCESS = 5;
    public static final int STATE_FAIL = 6;

    public static final int DETAIL_STATE_INIT = 1;
    public static final int DETAIL_STATE_SUCCESS = 2;
    public static final int DETAIL_STATE_FAIL = 3;

    public static final int PARSE_RETRY_TIMES = 5;

    public static final int DOWNLOAD_THREAD_NUM = 10;
    public static final int DOWNLOAD_RETRY_TIMES = 5;
    public static final String DOWNLOAD_TMP_DIR = "/tmp/m3u8downloader";

    public static final int MERGE_RETRY_TIMES = 1;
    public static final String MERGE_DIR = "/tmp/m3u8downloader";

    public static final int FORMAT_RETRY_TIMES = 1;
    public static final String FORMAT_CMD_PATH = "/usr/bin/ffmpeg";
    public static final String DOWNLOAD_DIR = "/sharedfolders/download";
}
