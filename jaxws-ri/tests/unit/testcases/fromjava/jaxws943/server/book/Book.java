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

package fromjava.jaxws943.server.book;

import com.sun.xml.ws.developer.Stateful;
import com.sun.xml.ws.developer.StatefulWebServiceManager;
import com.sun.xml.ws.developer.StatefulWebServiceManager.Callback;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.soap.Addressing;
import java.util.ArrayList;
import java.util.List;

/**
 * A book instance.
 *
 * @since 2.1 EA2
 */
@Stateful
@WebService(portName = "BookPort", targetNamespace = "http://client.jaxws943.fromjava/book") // target namespace is used for target java package
@Addressing
/**
 * Class to be used for testing stateful WS; Class represents a book with unique id and reviews submitted by users.
 */
public class Book {
    /**
     * The ID of the book.
     * In this example, it's used just to show that we are indeed
     * maintaining multiple instances and requests are dispatched
     * to the right instance.
     */
    private final String id;

    public static final Callback<Book> cbh = (Callback<Book>) new BookCallback();

    /**
     * This object is injected by the JAX-WS RI, and exposes various
     * operations to support stateful web services. Consult its javadoc
     * for more information.
     */
    public static StatefulWebServiceManager<Book> manager;
    public static volatile boolean timeoutSet;


    /**
     * Reviews of this book.
     *
     * <p>
     * The beauty of a stateful web service support is that you can
     * use instance fields like this to store object state.
     * In the real world, you'd probably be persisting this object
     * to database by perhaps using JPA, but this example doesn't show that.
     *
     * <p>
     * To allow concurrenct access, we use a copy-on-write pattern here,
     * hence 'volatile'.
     */
    private volatile List<String> reviews = new ArrayList<String>();

    // unlike stateless web service, the JAX-WS RI will never
    // create an instance. So you can define a constructor with arguments,
    // and in fact you'd normally want to do that so that you can initialize
    // the object with proper state.
    public Book(String id) {
        if (!timeoutSet) {
            manager.setTimeout(5000, cbh);
            timeoutSet = true;
        }
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Posts a review.
     *
     * <p>
     * Synchronized to handle concurrent submissions.
     */
    public synchronized void postReview(String review) {
        List<String> l = new ArrayList<String>(reviews);
        l.add(review);
        reviews = l;
    }

    /**
     * Gets all the reviews posted so far.
     */
    public List<String> getReviews() {
        return reviews;
    }

    /**
     * The equals implementation so that two {@link Book}s with the same ID
     * won't be exported twice.
     */
    @WebMethod(exclude = true)
    public boolean equals(Object that) {
        if (that == null || this.getClass() != that.getClass()) return false;
        return this.id.equals(((Book) that).id);
    }

    /**
     * Hashcode implementation consistent with {@link #equals(Object)}.
     */

    @WebMethod(exclude = true)
    public int hashCode() {
        return id.hashCode();
    }

}
