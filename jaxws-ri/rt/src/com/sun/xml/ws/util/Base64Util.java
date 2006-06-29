/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.util;

/**
 *
 * @author jitu
 */
public class Base64Util {
    
    protected static final char encodeBase64[] = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    protected static final int decodeBase64[] = {
        /*'+'*/ 62,
        -1, -1, -1,
        /*'/'*/ 63,
        /*'0'*/ 52,
        /*'1'*/ 53,
        /*'2'*/ 54,
        /*'3'*/ 55,
        /*'4'*/ 56,
        /*'5'*/ 57,
        /*'6'*/ 58,
        /*'7'*/ 59,
        /*'8'*/ 60,
        /*'9'*/ 61,
        -1, -1, -1, -1, -1, -1, -1,
        /*'A'*/ 0,
        /*'B'*/ 1,
        /*'C'*/ 2,
        /*'D'*/ 3,
        /*'E'*/ 4,
        /*'F'*/ 5,
        /*'G'*/ 6,
        /*'H'*/ 7,
        /*'I'*/ 8,
        /*'J'*/ 9,
        /*'K'*/ 10,
        /*'L'*/ 11,
        /*'M'*/ 12,
        /*'N'*/ 13,
        /*'O'*/ 14,
        /*'P'*/ 15,
        /*'Q'*/ 16,
        /*'R'*/ 17,
        /*'S'*/ 18,
        /*'T'*/ 19,
        /*'U'*/ 20,
        /*'V'*/ 21,
        /*'W'*/ 22,
        /*'X'*/ 23,
        /*'Y'*/ 24,
        /*'Z'*/ 25,
        -1, -1, -1, -1, -1, -1,
        /*'a'*/ 26,
        /*'b'*/ 27,
        /*'c'*/ 28,
        /*'d'*/ 29,
        /*'e'*/ 30,
        /*'f'*/ 31,
        /*'g'*/ 32,
        /*'h'*/ 33,
        /*'i'*/ 34,
        /*'j'*/ 35,
        /*'k'*/ 36,
        /*'l'*/ 37,
        /*'m'*/ 38,
        /*'n'*/ 39,
        /*'o'*/ 40,
        /*'p'*/ 41,
        /*'q'*/ 42,
        /*'r'*/ 43,
        /*'s'*/ 44,
        /*'t'*/ 45,
        /*'u'*/ 46,
        /*'v'*/ 47,
        /*'w'*/ 48,
        /*'x'*/ 49,
        /*'y'*/ 50,
        /*'z'*/ 51
    };
    
    public static String encode(byte[] value)
        throws Exception {

        if (value == null) {
            return null;
        }
        if (value.length == 0) {
            return "";
        }
        int blockCount = value.length / 3;
        int partialBlockLength = value.length % 3;

        if (partialBlockLength != 0) {
            ++blockCount;
        }

        int encodedLength = blockCount * 4;
        StringBuffer encodedValue = new StringBuffer(encodedLength);

        int idx = 0;
        for (int i = 0; i < blockCount; ++i) {
            int b1 = value[idx++];
            int b2 = (idx < value.length) ? value[idx++] : 0;
            int b3 = (idx < value.length) ? value[idx++] : 0;

            if (b1 < 0) {
                b1 += 256;
            }
            if (b2 < 0) {
                b2 += 256;
            }
            if (b3 < 0) {
                b3 += 256;
            }

            char encodedChar;

            encodedChar = encodeBase64[b1 >> 2];
            encodedValue.append(encodedChar);

            encodedChar = encodeBase64[((b1 & 0x03) << 4) | (b2 >> 4)];
            encodedValue.append(encodedChar);

            encodedChar = encodeBase64[((b2 & 0x0f) << 2) | (b3 >> 6)];
            encodedValue.append(encodedChar);

            encodedChar = encodeBase64[b3 & 0x3f];
            encodedValue.append(encodedChar);
        }

        switch (partialBlockLength) {
            case 0 :
                // do nothing
                break;
            case 1 :
                encodedValue.setCharAt(encodedLength - 1, '=');
                encodedValue.setCharAt(encodedLength - 2, '=');
                break;
            case 2 :
                encodedValue.setCharAt(encodedLength - 1, '=');
                break;
        }

        return encodedValue.toString();
    }
    
}
