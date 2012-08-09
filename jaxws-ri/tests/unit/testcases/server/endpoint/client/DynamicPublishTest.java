package server.endpoint.client;

import java.io.File;
import java.io.InputStream;
import javax.xml.ws.Endpoint;

import testutil.PortAllocator;

import junit.framework.TestCase;

/**
 * @author lingling guo
 */
public class DynamicPublishTest extends TestCase{
	
    //Publish a webservice when data-binding files are generated dynamically.
    public void testDeployFail() throws Exception {
        Endpoint endpoint = Endpoint.create(new GenericWithEnums());
        endpoint.publish("http://localhost:" + PortAllocator.getFreePort() + "/GenericWithEnumsservice.org/GenericWithEnums");        
        endpoint.stop();
    }
}