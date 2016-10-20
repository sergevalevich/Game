package com.balinasoft.clever.util;


public class UrlFormatter {
    private static final String MEDIA = "media/photo?filename=";
    public static String getFileNameFrom(String url) {
        return url.substring(url.indexOf('=') + 1);
    }
    public static String getQuestionUrlFrom(String questionFile) {
        return ConstantsManager.BASE_URL
                + MEDIA
                + questionFile.substring(questionFile.indexOf('=') + 1);
    }
    public static String getNewsUrlFrom(String newsFile) {
        return ConstantsManager.BASE_URL + MEDIA + newsFile;
    }
}
