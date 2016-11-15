package org.mortbay.ijetty.util;

import android.text.TextUtils;

/**
 * Created by kristain on 16/3/15.
 */
public class StringUtils {
    /**
     * Returns true if the string is null or 0-length.
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(CharSequence str) {
        return TextUtils.isEmpty(str);
    }
}
