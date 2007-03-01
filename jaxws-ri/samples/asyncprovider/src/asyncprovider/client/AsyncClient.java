package asyncprovider.client;

public class AsyncClient {

    public static void main (String[] args) {
		Hello_Service service = new Hello_Service();
		Hello proxy = service.getHelloAsyncPort();

		test(proxy, "sync", "source");
		test(proxy, "sync", "bean");
		test(proxy, "async", "source");
		test(proxy, "async", "bean");

    }

	private static void test(Hello proxy, String arg, String extra) {
    	Hello_Type req = new Hello_Type();
    	req.setArgument(arg);
    	req.setExtra(extra);
		System.out.println("Invoking Web Service with = "+arg+","+extra);
    	HelloResponse response = proxy.hello(req, req);
		System.out.println("arg="+response.getArgument()+
			" extra="+response.getExtra());
	}
}
