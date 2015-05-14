package junit.framework;

/**
 * Created by miran on 02/03/15.
 */
public class TestCase {

    public TestCase() {}
    public TestCase(String name) {}

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
    }

    public static void assertEquals(String msg, Object a, Object b) {
        if (a == null) {
            if (b != null) {
                fail(msg);
            }
        } else {
            if (!a.equals(b))
                fail(msg);
        }
        pass(msg);
    }

    public static void assertEquals(Object a, Object b) {
        assertEquals("assertEquals", a, b);
    }

    // necessary for bytecode compatibility
    public static void assertEquals(String a, String b) {
        assertEquals("", a, b);
    }

    public static void assertFalse(boolean condition) {
        if (condition)
            fail("assertFalse failed!");
        pass("assertFalse");
    }

    public static void assertNull(String msg, Object nullObject) {
        if (nullObject != null)
            fail(msg);
        pass(msg);
    }

    public static void assertNotNull(String msg, Object object) {
        if (object == null)
            fail(msg);
        pass(msg);
    }

    public static void assertNull(Object nullObject) {
        assertNull("assertNull", nullObject);
    }

    public static void assertNotNull(Object object) {
        assertNotNull("assertNotNull", object);
    }

    public static void assertNotSame(String message, Object expected, Object actual) {
        if (expected == actual) {
            fail(message);
        } else {
            pass(message);
        }
    }

    public static void assertNotSame(Object expected, Object actual) {
        assertNotSame("assertNotSame", expected, actual);
    }

    public static void assertSame(String message, Object expected, Object actual) {
        if (expected == actual) {
            pass(message);
        } else {
            fail(message);
        }
    }

    public static void assertSame(Object expected, Object actual) {
        assertSame("assertSame", expected, actual);
    }

    public static void assertTrue(boolean condition) {
        assertTrue("assertTrue", condition);
    }

    public static void assertTrue(String msg, boolean condition) {
        if (!condition)
            fail(msg);
        pass(msg);
    }

    public static void fail() {
        fail("");
    }

    public static void fail(String s) {
        throw new RuntimeException("ERROR: " + s);
    }

    public static void pass(String msg) {
        System.out.println("PASSED: " + msg);
    }

}
