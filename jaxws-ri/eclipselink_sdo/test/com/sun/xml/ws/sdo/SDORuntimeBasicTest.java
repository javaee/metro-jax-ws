package com.sun.xml.ws.sdo;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import org.eclipse.persistence.sdo.helper.SDOHelperContext;
import org.jvnet.ws.databinding.DatabindingModeFeature;

import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.api.databinding.DatabindingFactory;
import com.sun.xml.ws.db.sdo.HelperContextResolver;
import com.sun.xml.ws.db.sdo.SDOContextWrapper;
import com.sun.xml.ws.db.sdo.SDOUtils;
import com.sun.xml.ws.db.sdo.SchemaInfo;
import com.sun.xml.ws.sdo.test.AddNumbersPortType;
import com.sun.xml.ws.sdo.test.AddNumbersServiceImpl;
import com.sun.xml.ws.sdo.test.HelloSDO_DocLitWrap;
import com.sun.xml.ws.sdo.test.HelloSDO_DocLitWrapImpl;
import com.sun.xml.ws.sdo.test.HelloSDO_ProxyInterface;
import com.sun.xml.ws.sdo.test.HelloSDO_ProxyInterfaceImpl;
import com.sun.xml.ws.sdo.test.helloSDO.MySDO;
import com.sun.xml.ws.spi.db.BindingContext;

import commonj.sdo.helper.HelperContext;

public class SDORuntimeBasicTest extends SDODatabindingTestBase {
    static public final String ECLIPSELINK_SDO = "eclipselink.sdo";
    
    static public DatabindingFactory factory  = DatabindingFactory.newInstance();
    
    protected DatabindingModeFeature databindingMode() {
        return new DatabindingModeFeature(ECLIPSELINK_SDO); 
    }

    public void testEchoSDO() throws Exception {
        Class<HelloSDO_ProxyInterface> sei = HelloSDO_ProxyInterface.class;
        Class<HelloSDO_ProxyInterfaceImpl> seb = HelloSDO_ProxyInterfaceImpl.class;
        DatabindingConfig srvConfig = new DatabindingConfig();
        final HelperContext shc = SDOHelperContext.getHelperContext("server");
        HelperContextResolver shcr = new HelperContextResolver() {

            //@Override
            public HelperContext getHelperContext(boolean isClient,
                    QName serviceName, Map<String, Object> properties) {
                return shc;
            }
            
        };
        File f = getSchema("MySDO.xsd");
        
        Set<SchemaInfo> schemas = SDOUtils.getSchemas(f);
        
        srvConfig.setEndpointClass(seb);
        DatabindingModeFeature dbm = databindingMode();
        WebServiceFeature[] features = { dbm };
        srvConfig.setFeatures(features); 
        srvConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_INFO, schemas);
        srvConfig.properties().put(SDOContextWrapper.SDO_HELPER_CONTEXT_RESOLVER, shcr);

        DatabindingConfig cliConfig = new DatabindingConfig();
        final HelperContext chc = SDOHelperContext.getHelperContext("client");//SDODatabindingContext.getLocalHelperContext();
        HelperContextResolver chcr = new HelperContextResolver() {

            //@Override
            public HelperContext getHelperContext(boolean isClient,
                    QName serviceName, Map<String, Object> properties) {
                return chc;
            }
            
        };
        cliConfig.setContractClass(sei);
        cliConfig.setFeatures(features);
        cliConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_INFO, schemas);
        cliConfig.properties().put(SDOContextWrapper.SDO_HELPER_CONTEXT_RESOLVER, chcr);
        
        HelloSDO_ProxyInterface proxy =  createProxy(sei, srvConfig, cliConfig, false);
        SDOUtils.defineSchema(chc, f);
        MySDO mySDO = createMySDO(chc, "Gigi", 20);
        Object obj = proxy.echoSDO(mySDO);
        assertTrue(obj instanceof MySDO);
        mySDO = (MySDO) obj;
        assertEquals(21, mySDO.getIntPart());
        assertEquals("Gary", mySDO.getStringPart());
        String wrapperName = srvConfig.properties().get(
                BindingContext.class.getName()).getClass().getName();
        assertTrue(wrapperName != null && wrapperName.endsWith("SDOContextWrapper"));
    }
    
    public void testAddNumbers() throws Exception {
        DatabindingConfig srvConfig = new DatabindingConfig();
        File f = getSchema("AddNumbers.xsd");
        Class<AddNumbersPortType> sei = AddNumbersPortType.class;
        Class<AddNumbersServiceImpl> seb = AddNumbersServiceImpl.class;

        final HelperContext chc = SDOHelperContext.getHelperContext("client");//SDODatabindingContext.getLocalHelperContext();
        HelperContextResolver chcr = new HelperContextResolver() {

            //@Override
            public HelperContext getHelperContext(boolean isClient,
                    QName serviceName, Map<String, Object> properties) {
                return chc;
            }
            
        };
        SDOUtils.defineSchema(chc, f);
        Set<SchemaInfo> schemas = SDOUtils.getSchemas(f);
        
        final HelperContext shc = SDOHelperContext.getHelperContext("server");
        HelperContextResolver shcr = new HelperContextResolver() {

            //@Override
            public HelperContext getHelperContext(boolean isClient,
                    QName serviceName, Map<String, Object> properties) {
                return shc;
            }
            
        };
        srvConfig.setEndpointClass(seb);
        DatabindingModeFeature dbm = databindingMode();
        WebServiceFeature[] features = { dbm };
        srvConfig.setFeatures(features); 
        srvConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_INFO, schemas);
        //srvConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_FILE, f);
        srvConfig.properties().put(SDOContextWrapper.SDO_HELPER_CONTEXT_RESOLVER, shcr);

        DatabindingConfig cliConfig = new DatabindingConfig();
        cliConfig.setContractClass(sei);
        cliConfig.setFeatures(features);
        cliConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_INFO, schemas);
        //cliConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_FILE, f);
        cliConfig.properties().put(SDOContextWrapper.SDO_HELPER_CONTEXT_RESOLVER, chcr);
        AddNumbersPortType proxy =  createProxy(sei, srvConfig, cliConfig, false);
        Object obj = proxy.addNumbers(4, -83);
        assertTrue(obj instanceof Integer);
        Integer resp = (Integer) obj;
        assertEquals(new Integer(-79), resp);
    }


    public void testEchoSDO_DocLitWrapNoWrapper() throws Exception {
        Class<HelloSDO_DocLitWrap>     sei = HelloSDO_DocLitWrap.class;
        Class<HelloSDO_DocLitWrapImpl> seb = HelloSDO_DocLitWrapImpl.class;
        DatabindingConfig srvConfig = new DatabindingConfig();
        final HelperContext shc = SDOHelperContext.getHelperContext("server");
        HelperContextResolver shcr = new HelperContextResolver() {
            public HelperContext getHelperContext(boolean isClient,
                    QName serviceName, Map<String, Object> properties) {
                return shc;
            }
            
        };
        File f = getSchema("MySDO.xsd");
        
        Set<SchemaInfo> schemas = SDOUtils.getSchemas(f);
        
        srvConfig.setEndpointClass(seb);
        DatabindingModeFeature dbm = databindingMode();
        WebServiceFeature[] features = { dbm };
        srvConfig.setFeatures(features); 
        srvConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_INFO, schemas);
        srvConfig.properties().put(SDOContextWrapper.SDO_HELPER_CONTEXT_RESOLVER, shcr);
        srvConfig.properties().put("com.sun.xml.ws.api.model.SuppressDocLitWrapperGeneration", true);

        DatabindingConfig cliConfig = new DatabindingConfig();
        final HelperContext chc = SDOHelperContext.getHelperContext("client");//SDODatabindingContext.getLocalHelperContext();
        HelperContextResolver chcr = new HelperContextResolver() {
            public HelperContext getHelperContext(boolean isClient,
                    QName serviceName, Map<String, Object> properties) {
                return chc;
            }
            
        };
        cliConfig.setContractClass(sei);
        cliConfig.setFeatures(features);
        cliConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_INFO, schemas);
        cliConfig.properties().put(SDOContextWrapper.SDO_HELPER_CONTEXT_RESOLVER, chcr);
        cliConfig.properties().put("com.sun.xml.ws.api.model.SuppressDocLitWrapperGeneration", true);
        
        HelloSDO_DocLitWrap proxy =  createProxy(sei, srvConfig, cliConfig, false);
        SDOUtils.defineSchema(chc, f);
        String s = "Gigi";
        int i = 20;
        MySDO mySDO = createMySDO(chc, s, i);
        assertEquals(s, proxy.returnS(mySDO));
        assertEquals(i, proxy.returnI(mySDO));
        Object obj = proxy.echoSDO(mySDO);
        assertTrue(obj instanceof MySDO);
        mySDO = (MySDO) obj;
        assertEquals(21, mySDO.getIntPart());
        assertEquals("Gary", mySDO.getStringPart());
        String wrapperName = srvConfig.properties().get(
                BindingContext.class.getName()).getClass().getName();
        assertTrue(wrapperName != null && wrapperName.endsWith("SDOContextWrapper"));
        {
            MySDO[] a = { createMySDO(chc, "a", 1), createMySDO(chc, "b", 2), createMySDO(chc, "c", 3) };        
            List<MySDO> lreq = Arrays.asList(a);
            List<String> lres = proxy.testList1(lreq);
            assertEquals(lres.size(), lreq.size());
        }
        {
            MySDO[] a = { createMySDO(chc, "a", 1), createMySDO(chc, "b", 2), createMySDO(chc, "c", 3) };        
            List<String> l1 = Arrays.asList( new String[]{"w", "x", "y", "z"});    
            List<MySDO> l2 = Arrays.asList(a);
            List<String> lres = proxy.testList2(l1, l2);
            assertEquals(lres.size(), l1.size() + l2.size());
        }
        {
            MySDO[] a = { createMySDO(chc, "g", 5), createMySDO(chc, "j", 6), createMySDO(chc, "k", 7), createMySDO(chc, "l", 8), createMySDO(chc, "h", 9) };
            String[] res = proxy.arrayList01(a);
            assertEquals(res.length, a.length);
        }
    }

    private MySDO createMySDO(HelperContext chc, String s, int i) {
        MySDO mySDO = (MySDO) chc.getDataFactory().create(MySDO.class);
        mySDO.setIntPart(i);
        mySDO.setStringPart(s);
        return mySDO;
    }
    
    static public Method findMethod(Class<?> c, String name) {
        for (Method m : c.getMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }
    
    private static final File getSchema(String schemaName) {
        return ResourceHelper.file("eclipselink_sdo", "test", "etc", "wsdl",
                schemaName);
    }

}
