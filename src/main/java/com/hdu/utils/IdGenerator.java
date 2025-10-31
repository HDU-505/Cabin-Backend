package com.hdu.utils;


import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private static final String CHARACTERS = "0123456789";
    private static final SecureRandom random = new SecureRandom();
    private static final AtomicInteger counter = new AtomicInteger(0);

    public static String generate20CharId() {
        StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < 20; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /*
     *   生成固定6位长度的消息ID
     *   格式：时间戳后3位 + 自增计数3位
     * */
    public static String generateStateMessageId() {
        // 时间戳后3位
        String timePart = String.valueOf(System.currentTimeMillis() % 1_000);

        // 计数器（取 3 位，不足补0）
        int count = counter.updateAndGet(i -> (i >= 999 ? 0 : i + 1));
        String countPart = String.format("%03d", count);

        return timePart + countPart; // 总长度 6
    }

}
