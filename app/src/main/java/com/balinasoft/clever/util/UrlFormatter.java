package com.balinasoft.clever.util;


public class UrlFormatter {
    public static String getFileNameFrom(String url) {
        return url.substring(url.indexOf('=') + 1);
    }
}