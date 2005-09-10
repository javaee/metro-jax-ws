/*
 * $Id: EncoderUtils.java,v 1.3 2005-09-10 19:47:37 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.xml.ws.encoding.simpletype;

/**
 *
 * @author WS Development Team
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
