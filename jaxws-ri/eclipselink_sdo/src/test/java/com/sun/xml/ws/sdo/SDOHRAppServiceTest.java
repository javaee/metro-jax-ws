/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.sdo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import org.eclipse.persistence.sdo.helper.SDOHelperContext;
import org.eclipse.persistence.sdo.helper.SDOXSDHelper;
import org.xml.sax.EntityResolver;
import com.oracle.webservices.api.databinding.DatabindingModeFeature;
import com.oracle.webservices.api.databinding.ExternalMetadataFeature;

import com.sun.xml.ws.sdo.sample.service.*;
import com.sun.xml.ws.sdo.sample.service.types.Dept;
import com.sun.xml.ws.sdo.sample.service.types.Emp;
import com.sun.xml.ws.sdo.sample.service.types.ProcessControl;
import com.sun.xml.ws.util.xml.XmlUtil;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.ComponentFeature;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.TransportTubeFactory;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.db.sdo.SDOContextWrapper;
import com.sun.xml.ws.db.sdo.SDOUtils;
import com.sun.xml.ws.db.sdo.SchemaInfo;
import commonj.sdo.helper.HelperContext;

public class SDOHRAppServiceTest extends SDODatabindingTestBase {
    static public final String ECLIPSELINK_SDO = "eclipselink.sdo";
    boolean debug = false;
    
    public void testSDO_HRAppService() throws Exception {
        Set<SchemaInfo> schemas = SDOUtils.getSchemas(getResource("wsdl/HRAppService.wsdl").getFile());
        DatabindingConfig srvConfig = new DatabindingConfig();
        Class<HRAppService> sei = HRAppService.class;
        Class<HRAppServiceImpl> seb = HRAppServiceImpl.class;
        SDOConfig cSdo = sdoConfig(schemas, false);
        SDOConfig sSdo = sdoConfig(schemas, true);
        srvConfig.setContractClass(sei);
        srvConfig.setEndpointClass(seb);
        DatabindingModeFeature dbm = new DatabindingModeFeature("eclipselink.sdo");
        WebServiceFeature[] features = { dbm };
        srvConfig.setFeatures(features); 
        srvConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_INFO, schemas);
        //srvConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_FILE, f);
        srvConfig.properties().put(SDOContextWrapper.SDO_HELPER_CONTEXT_RESOLVER, sSdo.resolver);
        srvConfig.properties().put("com.sun.xml.ws.api.model.SuppressDocLitWrapperGeneration", true);

        DatabindingConfig cliConfig = new DatabindingConfig();
        cliConfig.setContractClass(sei);
        cliConfig.setFeatures(features);
        cliConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_INFO, schemas);
        //cliConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_FILE, f);
        cliConfig.properties().put(SDOContextWrapper.SDO_HELPER_CONTEXT_RESOLVER, cSdo.resolver);
        cliConfig.properties().put("com.sun.xml.ws.api.model.SuppressDocLitWrapperGeneration", true);
        HRAppService proxy =  createProxy(sei, srvConfig, cliConfig, debug);
        doTest(proxy, cSdo.context);
    }

    public void doTest(HRAppService proxy, HelperContext chc) throws Exception {
        java.math.BigDecimal totalComp = proxy.getTotalComp(new BigInteger("222"));
        assertEquals("222", totalComp.toString());
        totalComp = proxy.getTotalComp(new BigInteger("333"));
        assertEquals("333", totalComp.toString());

        doTestGetDeptno("1", proxy);
        doTestGetDeptno("2", proxy);
        doTestGetDeptno("2000", proxy);

        List<Emp> emps = proxy.getManagerAndPeers(new BigInteger("100"));
        assertEquals(3, emps.size());
        for (int i = 0; i < emps.size(); i++) {
            assertEquals("name" + i, emps.get(i).getEname());
            assertEquals("job" + i, emps.get(i).getJob());
        }
        HelperContext context = chc;
        {
            Emp emp = (Emp) context.getDataFactory().create(Emp.class);
            ProcessControl processControl = (ProcessControl) context.getDataFactory().create(ProcessControl.class);
            processControl.setReturnMode("Full");
    
            emp.setEname("name0");
            List<Emp> empIn = new Vector<Emp>();
            empIn.add(emp);
    
            emp = (Emp) context.getDataFactory().create(Emp.class);
            emp.setEname("name1");
            empIn.add(emp);
    
    
            List<Emp> ret = proxy.processEmps("myoperation", empIn, processControl);
    
            for (int i = 0; i < ret.size(); i++) {
                assertEquals(ret.get(i).getEname(), empIn.get(i).getEname());
                assertEquals("myoperation", ret.get(i).getJob());
    
            }
        }
        {

            Emp emp = createEmployee(200, "1", "name", "1000", "fry cook", context);

            Emp ret = proxy.createEmp(emp);
            assertEquals(ret.getEname(), emp.getEname());
            assertEquals(ret.getEmpno(), emp.getEmpno());
            assertEquals(ret.getDeptno(), emp.getDeptno());
            assertEquals(ret.getJob(), emp.getJob());
            doTestGetDeptno("1", proxy);
            doTestGetDeptno("2", proxy);
            doTestGetDeptno("2000", proxy);
        }
    }
    
    private void doTestGetDeptno(String stringVal, HRAppService proxy) {
        BigInteger val = new BigInteger(stringVal);
        Dept dept = proxy.getDept(val);
        assertEquals(val, dept.getDeptno());
    }
    
    private void doTestGetDeptno(String stringVal, HRAppServiceNoWrapper proxy) {
        BigInteger val = new BigInteger(stringVal);
        Dept dept = proxy.getDept(val);
        assertEquals(val, dept.getDeptno());
    }

    private Emp createEmployee(int comm, String deptNo, String name, String empNo, String job, HelperContext context) {
        Emp emp = (Emp) context.getDataFactory().create(Emp.class);
        emp.setComm(new BigDecimal(comm));
        emp.setDeptno(new BigInteger(deptNo));
        emp.setEname(name);
        emp.setEmpno(new BigInteger(empNo));
        emp.setJob(job);
        return emp;
    }
    
    //Bug 14071356
    public void testSDO_HRAppServiceNoWrapper() throws Exception {
        Set<SchemaInfo> schemas = SDOUtils.getSchemas(getResource("wsdl/HRAppService.wsdl").getFile());
        DatabindingConfig srvConfig = new DatabindingConfig();
        Class<HRAppServiceNoWrapper> sei = HRAppServiceNoWrapper.class;
        Class<HRAppServiceImpl>      seb = HRAppServiceImpl.class;
        SDOConfig cSdo = sdoConfig(schemas, false);
        SDOConfig sSdo = sdoConfig(schemas, true);
        srvConfig.setContractClass(sei);
        srvConfig.setEndpointClass(seb);
        DatabindingModeFeature dbm = new DatabindingModeFeature("eclipselink.sdo");
        WebServiceFeature[] features = { dbm };
        srvConfig.setFeatures(features); 
        srvConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_INFO, schemas);
        //srvConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_FILE, f);
        srvConfig.properties().put(SDOContextWrapper.SDO_HELPER_CONTEXT_RESOLVER, sSdo.resolver);
        srvConfig.properties().put("com.sun.xml.ws.api.model.SuppressDocLitWrapperGeneration", true);

        DatabindingConfig cliConfig = new DatabindingConfig();
        cliConfig.setContractClass(sei);
        cliConfig.setFeatures(features);
        cliConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_INFO, schemas);
        //cliConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_FILE, f);
        cliConfig.properties().put(SDOContextWrapper.SDO_HELPER_CONTEXT_RESOLVER, cSdo.resolver);
        cliConfig.properties().put("com.sun.xml.ws.api.model.SuppressDocLitWrapperGeneration", true);
        HRAppServiceNoWrapper proxy =  createProxy(sei, srvConfig, cliConfig, debug);
        


        java.math.BigDecimal totalComp = proxy.getTotalComp(new BigInteger("222"));
        assertEquals("222", totalComp.toString());
        totalComp = proxy.getTotalComp(new BigInteger("333"));
        assertEquals("333", totalComp.toString());

        doTestGetDeptno("1", proxy);
        doTestGetDeptno("2", proxy);
        doTestGetDeptno("2000", proxy);

        List<Emp> emps = proxy.getManagerAndPeers(new BigInteger("100"));
        assertEquals(3, emps.size());
        for (int i = 0; i < emps.size(); i++) {
            assertEquals("name" + i, emps.get(i).getEname());
            assertEquals("job" + i, emps.get(i).getJob());
        }
        HelperContext context = cSdo.context;
        {
            Emp emp = (Emp) context.getDataFactory().create(Emp.class);
            ProcessControl processControl = (ProcessControl) context.getDataFactory().create(ProcessControl.class);
            processControl.setReturnMode("Full");
    
            emp.setEname("name0");
            List<Emp> empIn = new Vector<Emp>();
            empIn.add(emp);
    
            emp = (Emp) context.getDataFactory().create(Emp.class);
            emp.setEname("name1");
            empIn.add(emp);
    
    
            List<Emp> ret = proxy.processEmps("myoperation", empIn, processControl);
    
            for (int i = 0; i < ret.size(); i++) {
                assertEquals(ret.get(i).getEname(), empIn.get(i).getEname());
                assertEquals("myoperation", ret.get(i).getJob());
    
            }
        }
        {

            Emp emp = createEmployee(200, "1", "name", "1000", "fry cook", context);

            Emp ret = proxy.createEmp(emp);
            assertEquals(ret.getEname(), emp.getEname());
            assertEquals(ret.getEmpno(), emp.getEmpno());
            assertEquals(ret.getDeptno(), emp.getDeptno());
            assertEquals(ret.getJob(), emp.getJob());
            doTestGetDeptno("1", proxy);
            doTestGetDeptno("2", proxy);
            doTestGetDeptno("2000", proxy);
        }
    }

    //Bug 14071356
    public void testSDO_HRAppServiceNoWrapperBug() throws Exception {
        Set<SchemaInfo> schemas = SDOUtils.getSchemas(getResource("wsdl/HRAppServiceBug.wsdl").getFile());
        DatabindingConfig srvConfig = new DatabindingConfig();
        Class<HRAppServiceNoWrapperBug> sei = HRAppServiceNoWrapperBug.class;
        Class<HRAppServiceImpl>      seb = HRAppServiceImpl.class;
        SDOConfig cSdo = sdoConfig(schemas, false);
        SDOConfig sSdo = sdoConfig(schemas, true);
        srvConfig.setContractClass(sei);
        srvConfig.setEndpointClass(seb);
        DatabindingModeFeature dbm = new DatabindingModeFeature("eclipselink.sdo");
        WebServiceFeature[] features = { dbm };
        srvConfig.setFeatures(features); 
        srvConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_INFO, schemas);
        //srvConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_FILE, f);
        srvConfig.properties().put(SDOContextWrapper.SDO_HELPER_CONTEXT_RESOLVER, sSdo.resolver);
        srvConfig.properties().put("com.sun.xml.ws.api.model.SuppressDocLitWrapperGeneration", true);

        DatabindingConfig cliConfig = new DatabindingConfig();
        cliConfig.setContractClass(sei);
        cliConfig.setFeatures(features);
        cliConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_INFO, schemas);
        //cliConfig.properties().put(SDOContextWrapper.SDO_SCHEMA_FILE, f);
        cliConfig.properties().put(SDOContextWrapper.SDO_HELPER_CONTEXT_RESOLVER, cSdo.resolver);
        cliConfig.properties().put("com.sun.xml.ws.api.model.SuppressDocLitWrapperGeneration", true);
        HRAppServiceNoWrapperBug proxy = createProxy(sei, srvConfig, cliConfig, true);

        java.math.BigDecimal totalComp = proxy.getTotalComp(new BigInteger("222"));
        assertEquals("222", totalComp.toString());
        totalComp = proxy.getTotalComp(new BigInteger("333"));
        assertEquals("333", totalComp.toString());
    }
    
    public void testSDO_HRAppServiceInVmTransport() throws Exception {
        final URL wsdlURL = getResource("wsdl/HRAppService.wsdl");
        Set<SchemaInfo> schemas = SDOUtils.getSchemas(wsdlURL.getFile());
        String tns = "http://sdo.sample.service/";  
        HRAppService_Service srv = new HRAppService_Service(wsdlURL, invmSetup(wsdlURL, HRAppService.class, HRAppServiceImpl.class, new QName(tns, "HRAppService"), new QName(tns, "HRAppServiceSoapHttpPort")));  
        HRAppService proxy = srv.getHRAppServiceSoapHttpPort();
        doTest(proxy, sdoConfig(schemas, false).context);
    }    
}
