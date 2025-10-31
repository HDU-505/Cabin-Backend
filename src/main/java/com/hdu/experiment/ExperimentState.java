package com.hdu.experiment;

public enum ExperimentState {
    NOT_STARTED,   // 未开始
    PREPARING,     // 准备中
    RUNNING,       // 进行中
    PAUSED,        // 暂停
    ENDED,         // 结束
    ERROR          // 异常
}
