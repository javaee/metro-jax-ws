/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.util;

import java.util.StringTokenizer;


/**
 * Provides some version utilities.
 *
 * @author JAX-WS Development Team
 */

public final class VersionUtil {

    public static boolean isVersion20(String version) {
        return JAXWS_VERSION_20.equals(version);
    }

    /**
     * @param version
     * @return true if version is a 2.0 version
     */
    public static boolean isValidVersion(String version) {
        return isVersion20(version);
    }

    public static String getValidVersionString() {
        return JAXWS_VERSION_20;
    }

    /**
     * BugFix# 4948171
     * Method getCanonicalVersion.
     *
     * Converts a given version to the format "a.b.c.d"
     * a - major version
     * b - minor version
     * c - minor minor version
     * d - patch version
     *
     * @return int[] Canonical version number
     */
    public static int[] getCanonicalVersion(String version) {
        int[] canonicalVersion = new int[4];

        // initialize the default version numbers
        canonicalVersion[0] = 1;
        canonicalVersion[1] = 1;
        canonicalVersion[2] = 0;
        canonicalVersion[3] = 0;

        final String DASH_DELIM = "_";
        final String DOT_DELIM = ".";

        StringTokenizer tokenizer =
                new StringTokenizer(version, DOT_DELIM);
        String token = tokenizer.nextToken();

        // first token is major version and must not have "_"
        canonicalVersion[0] = Integer.parseInt(token);

        // resolve the minor version
        token = tokenizer.nextToken();
        if (token.indexOf(DASH_DELIM) == -1) {
            // a.b
            canonicalVersion[1] = Integer.parseInt(token);
        } else {
            // a.b_c
            StringTokenizer subTokenizer =
                    new StringTokenizer(token, DASH_DELIM);
            canonicalVersion[1] = Integer.parseInt(subTokenizer.nextToken());
            // leave minorMinor default

            canonicalVersion[3] = Integer.parseInt(subTokenizer.nextToken());
        }

        // resolve the minorMinor and patch version, if any
        if (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            if (token.indexOf(DASH_DELIM) == -1) {
                // minorMinor
                canonicalVersion[2] = Integer.parseInt(token);

                // resolve patch, if any
                if (tokenizer.hasMoreTokens())
                    canonicalVersion[3] = Integer.parseInt(tokenizer.nextToken());
            } else {
                // a.b.c_d
                StringTokenizer subTokenizer =
                        new StringTokenizer(token, DASH_DELIM);
                // minorMinor
                canonicalVersion[2] = Integer.parseInt(subTokenizer.nextToken());

                // patch
                canonicalVersion[3] = Integer.parseInt(subTokenizer.nextToken());
            }
        }

        return canonicalVersion;
    }

    /**
     *
     * @param version1
     * @param version2
     * @return -1, 0 or 1 based upon the comparison results
     * -1 if version1 is less than version2
     * 0 if version1 is equal to version2
     * 1 if version1 is greater than version2
     */
    public static int compare(String version1, String version2) {
        int[] canonicalVersion1 = getCanonicalVersion(version1);
        int[] canonicalVersion2 = getCanonicalVersion(version2);

        if (canonicalVersion1[0] < canonicalVersion2[0]) {
            return -1;
        } else if (canonicalVersion1[0] > canonicalVersion2[0]) {
            return 1;
        } else {
            if (canonicalVersion1[1] < canonicalVersion2[1]) {
                return -1;
            } else if (canonicalVersion1[1] > canonicalVersion2[1]) {
                return 1;
            } else {
                if (canonicalVersion1[2] < canonicalVersion2[2]) {
                    return -1;
                } else if (canonicalVersion1[2] > canonicalVersion2[2]) {
                    return 1;
                } else {
                    if (canonicalVersion1[3] < canonicalVersion2[3]) {
                        return -1;
                } else if (canonicalVersion1[3] > canonicalVersion2[3]) {
                    return 1;
                } else
                    return 0;
                }
            }
        }
    }

    public static final String JAXWS_VERSION_20 = "2.0";
    // the latest version is default
    public static final String JAXWS_VERSION_DEFAULT = JAXWS_VERSION_20;
}
