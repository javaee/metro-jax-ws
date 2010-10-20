/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.transport.http.client;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A parser for date strings commonly found in http and email headers that
 * follow various RFC conventions.  Given a date-string, the parser will
 * attempt to parse it by trying matches with a set of patterns, returning
 * null on failure, a Date object on success.
 *
 * @author WS Development Team
 */
final class RfcDateParser {
    private boolean isGMT = false;
    static final String[] standardFormats =
        { "EEEE', 'dd-MMM-yy HH:mm:ss z", // RFC 850 (obsoleted by 1036)
        "EEEE', 'dd-MMM-yy HH:mm:ss", // ditto but no tz. Happens too often
        "EEE', 'dd-MMM-yyyy HH:mm:ss z", // RFC 822/1123
        "EEE', 'dd MMM yyyy HH:mm:ss z", // REMIND what rfc? Apache/1.1
        "EEEE', 'dd MMM yyyy HH:mm:ss z", // REMIND what rfc? Apache/1.1
        "EEE', 'dd MMM yyyy hh:mm:ss z", // REMIND what rfc? Apache/1.1
        "EEEE', 'dd MMM yyyy hh:mm:ss z", // REMIND what rfc? Apache/1.1
        "EEE MMM dd HH:mm:ss z yyyy", // Date's string output format
        "EEE MMM dd HH:mm:ss yyyy", // ANSI C asctime format()
        "EEE', 'dd-MMM-yy HH:mm:ss", // No time zone 2 digit year RFC 1123
        "EEE', 'dd-MMM-yyyy HH:mm:ss" // No time zone RFC 822/1123
    };
    static final String[] gmtStandardFormats =
        { "EEEE',' dd-MMM-yy HH:mm:ss 'GMT'", // RFC 850 (obsoleted by 1036)
        "EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'", // RFC 822/1123
        "EEE',' dd MMM yyyy HH:mm:ss 'GMT'", // REMIND what rfc? Apache/1.1
        "EEEE',' dd MMM yyyy HH:mm:ss 'GMT'", // REMIND what rfc? Apache/1.1
        "EEE',' dd MMM yyyy hh:mm:ss 'GMT'", // REMIND what rfc? Apache/1.1
        "EEEE',' dd MMM yyyy hh:mm:ss 'GMT'", // REMIND what rfc? Apache/1.1
        "EEE MMM dd HH:mm:ss 'GMT' yyyy" // Date's string output format
    };
    String dateString;

    public RfcDateParser(String dateString) {

        this.dateString = dateString.trim();

        if (this.dateString.indexOf("GMT") != -1) {
            isGMT = true;
        }
    }

    public Date getDate() {

        // format is wdy, DD-Mon-yyyy HH:mm:ss GMT
        int arrayLen =
            isGMT ? gmtStandardFormats.length : standardFormats.length;

        for (int i = 0; i < arrayLen; i++) {
            Date d;

            if (isGMT) {
                d = tryParsing(gmtStandardFormats[i]);
            } else {
                d = tryParsing(standardFormats[i]);
            }

            if (d != null) {
                return d;
            }
        }

        return null;
    }

    private Date tryParsing(String format) {

        SimpleDateFormat df = new SimpleDateFormat(format, Locale.US);

        if (isGMT) {
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
        }

        try {
            return df.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }
}
