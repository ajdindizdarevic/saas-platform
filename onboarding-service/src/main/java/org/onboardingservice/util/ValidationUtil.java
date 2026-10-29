package org.onboardingservice.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9]{7,15}$");

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) return true; // phone is optional
        return PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    public static String sanitize(String value) {
        if (value == null) return null;
        return value.trim().replaceAll("\\s+", " ");
    }
}