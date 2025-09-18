package com.hdu.config;

import org.springframework.stereotype.Component;

/*
*   cnosdb数据库配置
* */
@Component
public class CnosDBProperties {
    public static final String URL = "jdbc:arrow-flight-sql://127.0.0.1:8904";     //Cnosdb数据库部署地址
    public static final String DATABASE = "neurostudent2"; //Cnosdb数据库名
    public static final String USERNAME = "root"; //用户名
    public static final String PASSWORD = ""; //密码
    public static final String TENANT = "cnosdb";
    public static final boolean USE_ENCRYPTION = false;


    //下列是各种构建好的SQL语句
    //插入类
    public final static String EEG_INSERT = "INSERT INTO eeg (id, experiment_id, time, gnd, ref, afz, af3, af4, af7, af8, pz, p3, p4)";
    public final static String HR_INSERT = "INSERT INTO hr (id, experiment_id, time, data, num) ";
    public final static String GSR_INSERT = "INSERT INTO gsr (id, experiment_id, time, data, num) ";
    public final static String LABEL_INSERT = "INSERT INTO label (id, experiment_id, time, label, num) ";
    public final static String EOG_INSERT = "INSERT INTO eog (id, experiment_id, time, cnt, fpogx, fpogy, fpogs, fpogd, fpogid, fpogv, lpogx, lpogy, lpogv, rpogx, rpogy, rpogv, num) ";

}
