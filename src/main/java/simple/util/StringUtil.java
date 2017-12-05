package simple.util;

/**
 * 字符串工具类
 */
public final class StringUtil {

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        if (str != null) {
            str = str.trim();
        }
        return str == null || str.length() == 0;
    }

    /**
     * 判断字符串是否非空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}