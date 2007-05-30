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

import com.sun.xml.ws.developer.StatefulWebServiceManager;

import javax.jws.WebService;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

/**
 * The entry point to the book store web application.
 *
 * <p>
 * Let's say we are building an online book store. A natural modeling
 * of such application would involve in havling {@link Book} object
 * to represent each book. When you expose your bookstore as a web
 * service, it would be convenient to have a "remote reference" to
 * an individual book.
 *
 * <p>
 * {@link W3CEndpointReference} (EPR) is just that kind of remote reference.
 * You can turn a server-side {@link Book} object to a "remote reference"
 * by calling {@link StatefulWebServiceManager#export(Object)} and then
 * send it back to the client. The remote client can then use that EPR
 * to talk back to the exported {@link Book} object later. The client
 * can even pass that EPR to other web services, and have that service
 * talk to the exported {@link Book} instance.
 *
 * <p>
 * In a way, this works a bit like a distributed object system.
 *
 * @since 2.1 EA2
 */
@WebService
public class BookStore {
    /**
     * This web method is used by the client to obtain
     * a remote reference to a book instance.
     */
    public W3CEndpointReference getProduct(String id) {
        // in a real application, you'd probably be loading
        // such book instance from database, instead of
        // creating a new object.

        // when this method is called multiple times with the same ID,
        // the 2nd method invocation will return the EPR to the first
        // Book object created, because of Book.equals() implementation.

        // use the 'export' to turn an object reference into EPR.
        return Book.manager.export(new Book(id));

        // note that since there's no distributed GC, the exported object
        // remains in memory indefinitely. See StatefulWebServiceManager
        // javadoc for more about this, and how to avoid memory leak.
    }
}
