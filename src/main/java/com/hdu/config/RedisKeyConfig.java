package com.hdu.config;

/*
*   管理所有的redis的key值
* */
public interface RedisKeyConfig {
    public static final String EXPERIMENT_ON_OFF_STATUS = "experiment"; //实验状态前缀
    public static final String EXPERIMENT_STATUS_KEY_PREFIX = "experiment:status:"; //实验状态前缀
    public static final String EXPERIMENT_INFO_KEY_PREFIX = "experiment:info:";   //实验信息前缀
    public static final String EXPERIMENT_DATA_EEG_KEY = "data:eeg";   //脑电数据key
    public static final String EXPERIMENT_DATA_HR_KEY = "data:hr";   //心率数据key
    public static final String EXPERIMENT_DATA_GSR_KEY = "data:gsr";   //皮肤电数据key
    public static final String EXPERIMENT_DATA_EOG_KEY = "data:eog";   //眼动数据key
    public static final String EXPERIMENT_DATA_LABEL_KEY = "data:label";   //标签key

    // paradigm相关key值
    public static final String EXPERIMENT_PARADIGM_COVER_KEY = "paradigm:cover";
    public static final String EXPERIMENT_PARADIGM__FILE_KEY = "paradigm:file";

}
