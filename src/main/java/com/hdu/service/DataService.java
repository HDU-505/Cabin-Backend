package com.hdu.service;

import com.hdu.entity.*;

public interface DataService {
    // 数据中转
    public void dataDistribute(String topic,String data);
    //脑电数据缓存
    public void eegDataChache(EEG eeg);
    //皮肤电数据缓存
    public void gsrDataChache(GSR gsr);
    //心率数据缓存
    public void hrDataChache(HR hr);
    //眼动数据缓存
    public void eogDataChache(EOG eog);
    //标签数据缓存
    public void labelDataChache(Label label);
    //数据持久化操作
    public void dataStoreCnosDB();
    public void dataStoreMySQL();
}
