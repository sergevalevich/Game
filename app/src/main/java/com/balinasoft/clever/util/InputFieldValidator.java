package com.balinasoft.clever.util;

import org.androidannotations.annotations.EBean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EBean
public class InputFieldValidator {
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public boolean isPasswordValid(String password) {
        return password.length() > 0 && !password.contains(" ");
    }

    public boolean isEmailValid(String email) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
