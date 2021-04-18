package me.ping.bot.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6})$");

    public static boolean validateHexCode(String hex) {
        Matcher matcher = HEX_PATTERN.matcher(hex);
        return matcher.matches();
    }

    public static boolean hasCommand(String cmd, String str, boolean hasArgs) {
        if(hasArgs) {
            return str.toLowerCase().startsWith(cmd);
        } else {
            return str.equalsIgnoreCase(cmd);
        }
    }

    public static int countMatches(String str, String find) {
        int count = 0, fromIndex = 0;
        while ((fromIndex = str.indexOf(find, fromIndex)) != -1 ){
            count++;
            fromIndex++;
        }
        return count;
    }

    public static String removePrefix(final String text, String prefix) {
        if (isEmpty(text)) {
            return "";
        }
        if (prefix == null) {
            return text;
        }
        int textLength = text.length();
        int prefixLength = prefix.length();

        if (prefixLength == 0) {
            return text;
        }
        if (prefixLength > textLength) {
            return null;
        }

        return text.substring(prefix.length());
    }

    public static boolean startsWithIgnoreCase(final String text, final String prefix) {
        if (isEmpty(text)) {
            return false;
        }
        if (prefix == null) {
            return false;
        }
        int textLength = text.length();
        int prefixLength = prefix.length();
        if (prefixLength == 0) {
            return true;
        }
        if (prefixLength > textLength) {
            return false;
        }
        char[] chArray = prefix.toCharArray();
        for (int i = 0; i != chArray.length; ++i) {
            char ch1 = chArray[i];
            char ch2 = text.charAt(i);
            if (ch1 == ch2 || Character.toLowerCase(ch1) == Character.toLowerCase(ch2)) {
                // continue
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>
     * Returns whether the specified text is either empty or null.
     * </p>
     *
     * @param text The text to check; may be null;
     * @return True if the specified text is either empty or null.
     * @since 4.0
     */
    public static boolean isEmpty(final String text) {
        return (text == null || text.length() == 0);
    }

    public static String toLowerCase(String str) {
        String newStr = convertBasicLatinToLower(str);
        if (newStr == null) {
            return str.toLowerCase();
        }
        return newStr;
    }

    private static String convertBasicLatinToLower(String str) {
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (isBasicLatinUpperCase(chars[i])) {
                chars[i] = (char) ('a' + (chars[i] - 'A'));
            } else if (!isBasicLatinChar(chars[i])) {
                return null;
            }
        }
        return new String(chars);
    }

    private static boolean isBasicLatinUpperCase(char c) {
        return c >= 'A' && c <= 'Z';
    }

    private static boolean isBasicLatinChar(char c) {
        return c <= '\u007F';
    }
}
