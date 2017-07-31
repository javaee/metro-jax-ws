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

package com.sun.xml.ws.db.toplink;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Object pool allocator that leverages a {@code ConcurrentLinkedQueue} for
 * synchronization.
 * 
 * @param <T>
 *            the type of the object to pool
 */
public abstract class ObjectPool<T> {

	private volatile SoftReference<ConcurrentLinkedQueue<T>> queueRef;

	/**
	 * Allocate an object from the pool or create a new one if we cannot get one
	 * from the queue.
	 * 
	 * @return the queued or newly-created object
	 */
	public final T allocate() {
		T value = derefQueue().poll();
		return (value != null ? value : newInstance());
	}

	/**
	 * Return an object to the pool.
	 * 
	 * @param value
	 *            the object being returned
	 */
	public final void replace(T value) {
		derefQueue().offer(value);
	}

	/**
	 * Subclasses must override the object creation method.
	 * 
	 * @return a new instance of the object.
	 */
	protected abstract T newInstance();

	private final ConcurrentLinkedQueue<T> derefQueue() {
		ConcurrentLinkedQueue<T> q;

		// Only enter sync block if queue not allocated or soft reference
		// to it is cleared.
		if (queueRef == null || (q = queueRef.get()) == null) {
			synchronized (this) {
				if (queueRef == null || (q = queueRef.get()) == null) {
					q = new ConcurrentLinkedQueue<T>();
					queueRef = new SoftReference<ConcurrentLinkedQueue<T>>(q);
				}
			}
		}
		return q;
	}
}
