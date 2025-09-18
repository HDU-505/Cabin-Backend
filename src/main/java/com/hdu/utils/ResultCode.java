package com.hdu.utils;

public interface ResultCode {

    public static Integer SUCCESS = 20000; //成功

    public static Integer ERROR = 20001; //失败

    public static Integer TOKEN_ILLEGAL = 50012; //不合法Token

    public static Integer TOKEN_EXPIRE = 50015; //TOKEN过期
}
