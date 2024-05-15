package com.hamsterwhat.myreviews.utils;

public abstract class RegexPatterns {
    /**
     * Phone Number Regex
     */
    public static final String PHONE_REGEX = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";

    /**
     * Email Regex
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";

    /**
     * Password Regex。4-32 characters including alphabets, numbers, underscore
     */
    public static final String PASSWORD_REGEX = "^\\w{4,32}$";

    /**
     * Verification Code Regex, 6 characters including alphabets and numbers
     */
    public static final String VERIFY_CODE_REGEX = "^[a-zA-Z\\d]{6}$";

}
