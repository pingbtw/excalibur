package me.ping.bot.core;

public class StringUtils {
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
