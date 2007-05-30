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

package stateful.client;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Stateful web service client demonstration.
 */
public class Main {
    public static void main(String[] args) {
        BookStore bookstore = new BookStoreService().getBookStorePort();
        BookService service = new BookService();

        // obtain two book references
        Book book1 = service.getPort(bookstore.getProduct("abc001"),Book.class);
        Book book2 = service.getPort(bookstore.getProduct("def999"),Book.class);

        // you'll see that two proxies do represent two different server objects
        System.out.println("--- testing ID ---");
        System.out.println(book1.getId());
        System.out.println(book2.getId());

        // and as you see the server can maintain state on each book.
        // also notice that when you run the application repeatedly, the reviews accumulate.
        book1.postReview("This book is great!");
        book2.postReview("Recommend only for people with too much time");

        System.out.println("--- testing review ---");
        System.out.println(book1.getReviews());
        System.out.println(book2.getReviews());

        // if you obtain the 2nd reference to the same book, it still points to the same book
        Book book3 = service.getPort(bookstore.getProduct("abc001"),Book.class);
        System.out.println(book3.getReviews());




        // ... now we are done with basics, and moving on to a little more advanced topic.




        // on the wire, a reference to a remote object is captured
        // in an XML fragment called 'EndpointReference'.
        W3CEndpointReference bookEpr = bookstore.getProduct("def999");

        // since this is just an XML, you can save it to anywhere.
        // the following shows how to "save" it to System.out (so that you can see what's in it,
        // even though you don't really need to know what's in there)
        System.out.println("--- testing EPR ---");
        bookEpr.writeTo(new StreamResult(System.out));
        System.out.println();

        // let's save it to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bookEpr.writeTo(new StreamResult(baos));

        // it can be then read back to EPR later, maybe even from another JVM
        bookEpr = new W3CEndpointReference(new StreamSource(new ByteArrayInputStream(baos.toByteArray())));
        // ... and then to Book proxy
        Book book4 = service.getPort(bookEpr, Book.class);
        // ... to talk to this book
        System.out.println(book4.getReviews());
    }
}
