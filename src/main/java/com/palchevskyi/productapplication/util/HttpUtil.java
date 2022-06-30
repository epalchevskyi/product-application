package com.palchevskyi.productapplication.util;

public class HttpUtil {
    public static String buildUrl(String hostname, String port) {
        return "http://" + hostname + ":" + port;
    }
}
