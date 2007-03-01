package wsimport_catalog;

public class WsimportCatalogTester{
    public static void main (String[] args) {
        testCatalog();
    }

    /**
     * Just check if the class is loaded, meaning both -p worked and catalog resolver worked too
     */
    public static void testCatalog(){
        try {
            Class cls = Class.forName("wsimport_catalog.Hello");
            System.out.println("wsimport_catalog sample: Succeessfuly resolved wsdl and schema and generated artifacts!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("wsimport_catalog sample failed!");
        }
    }
}
