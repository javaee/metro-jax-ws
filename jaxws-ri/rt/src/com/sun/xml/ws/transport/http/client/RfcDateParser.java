/*
 * $Id: RfcDateParser.java,v 1.3 2005-09-10 19:48:06 kohsuke Exp $
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

package com.sun.xml.ws.transport.http.client;

import java.text.SimpleDateFormat;
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
public class RfcDateParser {

    private static final String debugProp = "hotjava.debug.RfcDateParser";
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
        } catch (Exception e) {
            return null;
        }
    }
}
