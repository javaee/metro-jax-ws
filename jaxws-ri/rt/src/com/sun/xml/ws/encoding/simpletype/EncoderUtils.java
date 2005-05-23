/*
 * $Id: EncoderUtils.java,v 1.1 2005-05-23 22:28:40 bbissett Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.encoding.simpletype;

/**
 *
 * @author JAX-RPC Development Team
 */
public class EncoderUtils {
    public static boolean needsCollapsing(String str) {
        int len = str.length();
        int spanLen = 0;

        for (int idx = 0; idx < len; ++idx) {
            if (Character.isWhitespace(str.charAt(idx))) {
                ++spanLen;
            } else if (spanLen > 0) {
                if (spanLen == idx) {
                    // leading whitespace
                    return true;
                } else {
                    // non-leading, non-trailing whitespace
                    if (str.charAt(idx - spanLen) != ' ') {
                        // first whitespace character is not a space
                        return true;
                    }
                    if (spanLen > 1) {
                        // there is a span of multiple whitespace characters
                        return true;
                    }
                }

                spanLen = 0;
            }
        }

        if (spanLen > 0) {
            // trailing whitespace
            return true;
        }

        return false;
    }

    public static String collapseWhitespace(String str) {
        if (!needsCollapsing(str)) {
            return str;
        }

        // the assumption is that most strings will not need to be collapsed,
        // so the code below will usually not be reached

        int len = str.length();
        char[] buf = new char[len];
        str.getChars(0, len, buf, 0);

        int leadingWSLen = 0;
        int trailingWSLen = 0;
        int spanLen = 0;

        for (int idx = 0; idx < len; ++idx) {
            if (Character.isWhitespace(buf[idx])) {
                ++spanLen;
            } else if (spanLen > 0) {
                if (spanLen == idx) {
                    // leading whitespace
                    leadingWSLen = spanLen;
                } else {
                    // non-leading, non-trailing whitespace

                    // ensure that the first whitespace character is a space
                    int firstWSIdx = idx - spanLen;
                    buf[firstWSIdx] = ' ';

                    if (spanLen > 1) {
                        // remove all but the first whitespace character
                        System.arraycopy(
                            buf,
                            idx,
                            buf,
                            firstWSIdx + 1,
                            len - idx);
                        len -= (spanLen - 1);
                        idx = firstWSIdx + 1;
                    }
                }

                spanLen = 0;
            }
        }

        if (spanLen > 0) {
            // trailing whitespace
            trailingWSLen = spanLen;
        }

        return new String(
            buf,
            leadingWSLen,
            len - leadingWSLen - trailingWSLen);
    }

    public static String removeWhitespace(String str) {
        int len = str.length();
        StringBuffer buf = new StringBuffer();
        int firstNonWS = 0;
        int idx = 0;
        for (; idx < len; ++idx) {
            if (Character.isWhitespace(str.charAt(idx))) {
                if (firstNonWS < idx)
                    buf.append(str.substring(firstNonWS, idx));
                firstNonWS = idx + 1;
            }
        }
        if (firstNonWS < idx)
            buf.append(str.substring(firstNonWS, idx));
        return buf.toString();
    }
}
