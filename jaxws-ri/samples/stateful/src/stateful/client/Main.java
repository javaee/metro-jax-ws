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
