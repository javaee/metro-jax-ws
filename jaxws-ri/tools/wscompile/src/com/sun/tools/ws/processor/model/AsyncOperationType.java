/*
 * $Id: AsyncOperationType.java,v 1.1 2005-05-23 23:18:54 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.model;



/**
 * @author Vivek Pandey
 *
 * Async Operation type
 */
public final class AsyncOperationType {

    public static final AsyncOperationType POLLING = new AsyncOperationType();
    public static final AsyncOperationType CALLBACK = new AsyncOperationType();

    private AsyncOperationType() {
    }

}
