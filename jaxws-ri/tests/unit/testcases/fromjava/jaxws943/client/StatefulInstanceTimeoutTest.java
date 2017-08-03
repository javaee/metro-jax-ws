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

package fromjava.jaxws943.client;

import fromjava.jaxws943.client.book.*;

import junit.framework.TestCase;

import java.util.logging.Logger;

/**
 * Stateful web service client demonstration.
 */
public class StatefulInstanceTimeoutTest extends TestCase {

    private static final Logger logger = Logger.getLogger(StatefulInstanceTimeoutTest.class.getName());

    public void testIt() throws InterruptedException {
        BookStore bookstore = new BookStoreService().getBookStorePort();
        BookService service = new BookService();

        // obtain references to two differentbooks and add to the different nuber of reviews:
        // abc001 - 10 reviews
        // abc002 - 5 reviews
        Book book;
        book = service.getPort(bookstore.getProduct("abc001"),Book.class);

        for(int i=0; i < 10; i++) {

            // timeout (timeout defined as 5secs)
            // - if the first callback isn't canceled (bug JAXWS_943), it will fail sometimes during this loop
            logger.finest("waiting 1 sec to reproduce timeout in case the very first callback invoked ...");
            Thread.sleep(1000L);

            book = service.getPort(bookstore.getProduct("abc001"),Book.class);
            book.postReview("review #" + i);

            if (i % 2 == 0) {
                book = service.getPort(bookstore.getProduct("abc002"),Book.class);
                book.postReview("review #" + i);
            }
        }

        // assert all the reviews are in the place:
        book = service.getPort(bookstore.getProduct("abc001"),Book.class);
        assertTrue(book.getReviews().size() == 10);
        logger.finest("book1 reviews = " + book.getReviews());

        book = service.getPort(bookstore.getProduct("abc002"),Book.class);
        assertTrue(book.getReviews().size() == 5);
        logger.finest("book2 reviews = " + book.getReviews());

        // timeout (timeout defined as 5secs)
        logger.finest("waiting to reproduce timeout ...");
        Thread.sleep(8000L);

        // after timeout we should get brand new books (old are trashed)
        book = service.getPort(bookstore.getProduct("abc001"),Book.class);
        assertTrue(book.getReviews().size() == 0);
        logger.finest("book1 reviews = " + book.getReviews());

        book = service.getPort(bookstore.getProduct("abc002"),Book.class);
        assertTrue(book.getReviews().size() == 0);
        logger.finest("book2 reviews = " + book.getReviews());
    }
}
