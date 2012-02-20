package fromjava.error_capture;

import javax.jws.WebService;

/**
 * @author Kohsuke Kawaguchi
 */
@WebService
public class MyService {
    /**
     * Throws an exception. We'll check the shape of the thrown exception on the client.
     */
    public void foo() {
        try {
            bar();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private void bar() {
        throw new NullPointerException();
    }
}
