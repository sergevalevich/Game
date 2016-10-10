package com.balinasoft.clever.util;


public class UrlFormatter {
    public static String getFileNameFrom(String url) {
        return url.substring(url.indexOf('=') + 1);
    }
    public static String getUrlFrom(String unFormattedUrl) {
        return ConstantsManager.BASE_URL
                + "media/photo?filename="
                + unFormattedUrl.substring(unFormattedUrl.indexOf('=') + 1);
    }
}
