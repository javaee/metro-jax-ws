/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package stateful.server;

import com.sun.xml.ws.developer.Stateful;
import com.sun.xml.ws.developer.StatefulWebServiceManager;

import javax.jws.WebService;
import javax.xml.ws.soap.Addressing;
import java.util.ArrayList;
import java.util.List;

/**
 * A book instance.
 *
 * @since 2.1 EA2
 */
@Stateful @WebService @Addressing
public class Book {
    /**
     * The ID of the book.
     * In this example, it's used just to show that we are indeed
     * maintaining multiple instances and requests are dispatched
     * to the right instance.
     */
    private final String id;

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
    public boolean equals(Object that) {
        if (that == null || this.getClass() != that.getClass()) return false;
        return this.id.equals(((Book)that).id);
    }

    /**
     * Hashcode implementation consistent with {@link #equals(Object)}.
     */
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * This object is injected by the JAX-WS RI, and exposes various
     * operations to support stateful web services. Consult its javadoc
     * for more information.
     */
    public static StatefulWebServiceManager<Book> manager;
}
