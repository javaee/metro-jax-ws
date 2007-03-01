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
