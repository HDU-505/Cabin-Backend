package com.hdu.utils.experiment;

/*
*   实验状态枚举
* */
public enum ExperimentStatus {
    WAITING,    //等待状态
    STARTED,    //开始状态
    TERMINATED, //异常终止状态
    ENDED   //结束状态
}