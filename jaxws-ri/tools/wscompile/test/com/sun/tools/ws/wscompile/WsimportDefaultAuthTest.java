package com.sun.tools.ws.wscompile;

import com.sun.istack.NotNull;
import com.sun.tools.ws.processor.modeler.wsdl.ConsoleErrorReporter;
import junit.framework.TestCase;
import java.io.File;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Arrays;

/**
 * @author Rama Pulavarthi
 */
public class WsimportDefaultAuthTest extends TestCase {

    private static class MyAuthenticator extends DefaultAuthenticator {

        public MyAuthenticator(@NotNull ErrorReceiver receiver, @NotNull File authfile) throws BadCommandLineException {
            super(receiver, authfile);
        }

        protected URL getRequestingURL() {
            try {
                return new URL("http://foo.com/myservice?wsdl");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void testDefaultAuth() throws Exception{
        URL url = getResourceAsUrl("com/sun/tools/ws/wscompile/.auth");
        DefaultAuthenticator da = new MyAuthenticator(new ConsoleErrorReporter(System.out), new File(url.toURI()));
        PasswordAuthentication pa = da.getPasswordAuthentication();
        assertTrue(pa != null && pa.getUserName().equals("duke") && Arrays.equals(pa.getPassword(), "test".toCharArray()));

    }

    private static URL getResourceAsUrl(String resourceName) throws RuntimeException {
        URL input = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (input == null) {
            throw new RuntimeException("Failed to find resource \"" + resourceName + "\"");
        }
        return input;
    }

}
