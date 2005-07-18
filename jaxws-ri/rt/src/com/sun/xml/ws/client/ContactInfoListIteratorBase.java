/*
 * $Id: ContactInfoListIteratorBase.java,v 1.2 2005-07-18 16:52:05 kohlert Exp $
 */

/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved.
*/
package com.sun.xml.ws.client;

import com.sun.pept.ept.ContactInfo;
import com.sun.pept.ept.ContactInfoListIterator;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author WS Development Team
 */
public class ContactInfoListIteratorBase implements ContactInfoListIterator {
    private Iterator iterator;

    public ContactInfoListIteratorBase(ArrayList list) {
        iterator = list.iterator();
    }

    public ContactInfo next() {
        return (ContactInfo) iterator.next();
    }

    /* (non-Javadoc)
     * @see com.sun.pept.ept.ContactInfoListIterator#hasNext()
     */
    public boolean hasNext() {
        return iterator.hasNext();
    }

}
