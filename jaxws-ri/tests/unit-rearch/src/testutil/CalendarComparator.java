/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package testutil;

import java.util.Date;
import java.util.Calendar;
import java.lang.reflect.*;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

/**
 *
 * @author JAX-RPC RI Development Team
 */
public class CalendarComparator extends junit.framework.Assert {

    public static boolean compareCalendars(Calendar cal1, Calendar cal2) throws Exception {
        if (cal1 == cal2)
            return true;
        if ((cal1 == null && cal2 != null) ||
            (cal1 != null && cal2 == null)) {
            return false;
        }
        if (!compareCalendarStrings(cal1, cal2)) {
            return false;
        }

        Method getDSTSavingsMethod = TimeZone.getDefault().getClass().getMethod("getDSTSavings", null);
	return cal1.getTime().equals(cal2.getTime());

/*        int offset1 = cal1.get(Calendar.ZONE_OFFSET)+(cal1.getTimeZone().inDaylightTime(cal1.getTime()) ?
                ((Integer)getDSTSavingsMethod.invoke(cal1.getTimeZone(), null)).intValue() : 0);
        int offset2 = cal2.get(Calendar.ZONE_OFFSET)+(cal2.getTimeZone().inDaylightTime(cal2.getTime()) ?
                ((Integer)getDSTSavingsMethod.invoke(cal2.getTimeZone(), null)).intValue() : 0);
		return (cal1.getTime().equals(cal2.getTime()) && offset1 == offset2);
*/
    }

	public static boolean compareCalendarByTime(Calendar cal1, Calendar cal2) throws Exception {
		if (cal1 == cal2)
			return true;
		if ((cal1 == null && cal2 != null) ||
			(cal1 != null && cal2 == null)) {
			return false;
		}

		return cal1.getTime().equals(cal2.getTime());
	}
	
    public static void assertCalendarsAreEqual(Calendar cal1, Calendar cal2) throws Exception {
        if (cal1 == cal2)
            return;

        if (cal1 == null) {
            assertTrue("expected null calendar", cal2 == null);
            return;
        } else {
            assertTrue("expected non-null calendar", cal2 != null);
        }

        String cal1String = getCalendarString(cal1);
        String cal2String = getCalendarString(cal2);
        if (!cal1String.equals(cal2String)) {
            assertTrue("Excpected calendar: "+cal1String+" but got: "+cal2String, false);
        }

        Method getDSTSavingsMethod = TimeZone.getDefault().getClass().getMethod("getDSTSavings", null);

/*        int offset1 = cal1.get(Calendar.ZONE_OFFSET)+(cal1.getTimeZone().inDaylightTime(cal1.getTime()) ?
                ((Integer)getDSTSavingsMethod.invoke(cal1.getTimeZone(), null)).intValue() : 0);
        int offset2 = cal2.get(Calendar.ZONE_OFFSET)+(cal2.getTimeZone().inDaylightTime(cal2.getTime()) ?
                ((Integer)getDSTSavingsMethod.invoke(cal2.getTimeZone(), null)).intValue() : 0);
*/
        if (!cal1.getTime().equals(cal2.getTime())) {
            assertTrue("Expected date of: "+cal1.getTime()+" but got: "+cal2.getTime(), false);
        }
/*        if (offset1 != offset2) {
            assertTrue("Expected an offset of: "+offset1+" but got: "+offset2, false);
        }*/
    }

    public static boolean compareCalendarStrings(Calendar cal1, Calendar cal2) throws Exception {
        boolean succeed = getCalendarString(cal1).equals(getCalendarString(cal2));
        if (!succeed) {
            System.out.println("cal1: \"" +getCalendarString(cal1) + "\"");
            System.out.println("cal2: \"" +getCalendarString(cal2) + "\"");
        }
        return succeed;
    }

    public static String getCalendarString(Calendar cal) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS z G");
        TimeZone tmpzone = cal.getTimeZone();
        tmpzone.setID("Custom");
        df.setTimeZone(tmpzone);
//System.out.println("******** String1="+df.format(cal.getTime()));
        return df.format(cal.getTime());
    }

    public static boolean compareCalendarArrays(Object[] arr1, Object[] arr2) throws Exception {
        boolean retVal = true;
        if (arr1.length != arr2.length)
            return false;
        for (int i=0; i< arr1.length && retVal; i++) {
            if (arr1[i] instanceof Date || arr2[i] instanceof Date) {
                Date date1;
                Date date2;
                if (arr1[i] instanceof Date)
                    date1 = (Date)arr1[i];
                else
                    date1 = ((Calendar)arr1[i]).getTime();
                if (arr2[i] instanceof Date)
                    date2 = (Date)arr2[i];
                else
                    date2 = ((Calendar)arr2[i]).getTime();
                retVal = date1.equals(date2);
            } else {
                retVal = compareCalendars((Calendar)arr1[i], (Calendar)arr2[i]);
            }
            if (!retVal) {
                System.out.println("arr1: " + arr1[i]);
                System.out.println();
                System.out.println("arr2: " + arr2[i]);
            }
        }
        return retVal;
    }

    public static boolean compareCalendarArraysByDate(Object[] arr1, Object[] arr2) throws Exception {
        boolean retVal = true;
        if (arr1.length != arr2.length)
            return false;
        for (int i=0; i< arr1.length && retVal; i++) {
            Date date1;
            Date date2;
            if (arr1[i] instanceof Date)
                date1 = (Date)arr1[i];
            else
                date1 = ((Calendar)arr1[i]).getTime();
            if (arr2[i] instanceof Date)
                date2 = (Date)arr2[i];
            else
                date2 = ((Calendar)arr2[i]).getTime();
            retVal = date1.equals(date2);
            if (!retVal) {
                System.out.println("arr1: " + arr1[i]);
                System.out.println();
                System.out.println("arr2: " + arr2[i]);
            }
        }
        return retVal;
    }
}
