/*
 * JAXWSServerAgent.java
 *
 * Created on February 7, 2007, 6:37 PM
 */

package com.sun.xml.ws.server;

import com.sun.xml.ws.util.RuntimeVersionMBean;
import com.sun.xml.ws.util.RuntimeVersion;
import com.sun.xml.ws.transport.http.HttpDump;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

/**
 *
 * @author Jitendra Kotamraju
 */
public class JMXAgent {

    // Platform MBeanServer used to register your MBeans
    private final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    
    // Singleton instance
    private static JMXAgent singleton;
    
    /**
     * Instantiate and register your MBeans.
     */
    public void init() throws Exception {    
        RuntimeVersionMBean mbean = new RuntimeVersion();
        ObjectName mbeanName = new ObjectName("com.sun.xml.ws.util:type=RuntimeVersion");
        getMBeanServer().registerMBean(mbean, mbeanName);

        HttpDump dump = new HttpDump();
        ObjectName dumpName = new ObjectName("com.sun.xml.ws.transport.http:type=HttpDump");
        getMBeanServer().registerMBean(dump, dumpName);
    }
    
    /**
     * Returns an agent singleton.
     */
    public synchronized static JMXAgent getDefault() throws Exception {
        if(singleton == null) {
            singleton = new JMXAgent();
            singleton.init();
        }
        return singleton;
    }
    
    private MBeanServer getMBeanServer() {
        return mbs;
    }
    
}
