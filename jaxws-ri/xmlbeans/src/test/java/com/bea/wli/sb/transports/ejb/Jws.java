package com.bea.wli.sb.transports.ejb;

import java.security.PrivilegedExceptionAction;
//import weblogic.jws.WLLocalTransport;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.jws.soap.SOAPBinding.ParameterStyle;
//import com.bea.wli.sb.transports.ejb.jwsgeneration.BaseJwsService;
import java.util.HashMap;

//import com.bea.xml.XmlObject;
import org.apache.xmlbeans.XmlObject;

//@WLLocalTransport(serviceUri="service.jws")
@WebService(targetNamespace="http://www.openuri.org/", serviceName="JwsService", name="Jws")@SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.WRAPPED)
public class Jws {
//HashMap props = null;
//public Jws(HashMap props)
//{
//this.props = props;
//}

        @WebMethod(action="http://www.openuri.org/addCountry")
        public com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument addCountry( @WebParam(name="arg0") final com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument arg0,  @WebParam(name="arg1") final com.bea.wli.sb.transports.ejb.test.xbean.CountryInfoType arg1) throws Exception
        {
//            while (true) {
//                BaseJwsService __jwsHelper = new BaseJwsService(props);
//                try {
//
//                    final com.bea.wli.sb.transports.ejb.test.xbean.EjbXBean __remote = 
//                    __jwsHelper.getRemote(com.bea.wli.sb.transports.ejb.test.xbean.EjbXBeanEJBHome.class,
//                    com.bea.wli.sb.transports.ejb.test.xbean.EjbXBean.class);
//
//                    PrivilegedExceptionAction<com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument> __action = new PrivilegedExceptionAction<com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument>() {
//                        public com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument run() throws Exception {
//                            com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument __result = __remote.addCountry(arg0, arg1);
//                            return __result;
//                        }
//                    };
//
//                    return __jwsHelper.invoke(__action);
//
//                } catch(javax.ejb.EJBException ex) {
//                    if (((Throwable)ex).getCause() instanceof java.rmi.NoSuchObjectException) {
//                        __jwsHelper.clearPool();
//                    }
//                    else {
//                        throw ex;
//                    } 
//                } finally {
//                    __jwsHelper.close();
//                }
//
//            }
            return null;
        }

//        @WebMethod(action="http://www.openuri.org/getAddress")
//        @WebResult(name="return")
//        public com.bea.wli.sb.transports.ejb.test.xbean.MyAddress getAddress( @WebParam(name="arg0") final java.lang.String arg0,  @WebParam(name="arg1") final java.lang.String arg1) throws Exception
//        {
////            while (true) {
////                BaseJwsService __jwsHelper = new BaseJwsService(props);
////                try {
////
////                    final com.bea.wli.sb.transports.ejb.test.xbean.EjbXBean __remote = 
////                    __jwsHelper.getRemote(com.bea.wli.sb.transports.ejb.test.xbean.EjbXBeanEJBHome.class,
////                    com.bea.wli.sb.transports.ejb.test.xbean.EjbXBean.class);
////
////                    PrivilegedExceptionAction<com.bea.wli.sb.transports.ejb.test.xbean.MyAddress> __action = new PrivilegedExceptionAction<com.bea.wli.sb.transports.ejb.test.xbean.MyAddress>() {
////                        public com.bea.wli.sb.transports.ejb.test.xbean.MyAddress run() throws Exception {
////                            com.bea.wli.sb.transports.ejb.test.xbean.MyAddress __result = __remote.getAddress(arg0, arg1);
////                            return __result;
////                        }
////                    };
////
////                    return __jwsHelper.invoke(__action);
////
////                } catch(javax.ejb.EJBException ex) {
////                    if (((Throwable)ex).getCause() instanceof java.rmi.NoSuchObjectException) {
////                        __jwsHelper.clearPool();
////                    }
////                    else {
////                        throw ex;
////                    } 
////                } finally {
////                    __jwsHelper.close();
////                }
////
////            }
//            return null;
//        }

        @WebMethod(action="http://www.openuri.org/getCountryInfo")
        @WebResult(name="return")
        public XmlObject getCountryInfo( @WebParam(name="arg0") final XmlObject arg0,  @WebParam(name="arg1") final java.lang.String arg1) throws Exception
        {
//            while (true) {
//                BaseJwsService __jwsHelper = new BaseJwsService(props);
//                try {
//
//                    final com.bea.wli.sb.transports.ejb.test.xbean.EjbXBean __remote = 
//                    __jwsHelper.getRemote(com.bea.wli.sb.transports.ejb.test.xbean.EjbXBeanEJBHome.class,
//                    com.bea.wli.sb.transports.ejb.test.xbean.EjbXBean.class);
//
//                    PrivilegedExceptionAction<com.bea.xml.XmlObject> __action = new PrivilegedExceptionAction<com.bea.xml.XmlObject>() {
//                        public com.bea.xml.XmlObject run() throws Exception {
//                            com.bea.xml.XmlObject __result = __remote.getCountryInfo(arg0, arg1);
//                            return __result;
//                        }
//                    };
//
//                    return __jwsHelper.invoke(__action);
//
//                } catch(javax.ejb.EJBException ex) {
//                    if (((Throwable)ex).getCause() instanceof java.rmi.NoSuchObjectException) {
//                        __jwsHelper.clearPool();
//                    }
//                    else {
//                        throw ex;
//                    } 
//                } finally {
//                    __jwsHelper.close();
//                }
//
//            }
            return null;
        }

        @WebMethod(action="http://www.openuri.org/getCountryName")
        @WebResult(name="return")
        public java.lang.String getCountryName( @WebParam(name="arg0") final com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument arg0,  @WebParam(name="arg1") final com.bea.wli.sb.transports.ejb.test.xbean.Code arg1) throws Exception
        {
//            while (true) {
//                BaseJwsService __jwsHelper = new BaseJwsService(props);
//                try {
//
//                    final com.bea.wli.sb.transports.ejb.test.xbean.EjbXBean __remote = 
//                    __jwsHelper.getRemote(com.bea.wli.sb.transports.ejb.test.xbean.EjbXBeanEJBHome.class,
//                    com.bea.wli.sb.transports.ejb.test.xbean.EjbXBean.class);
//
//                    PrivilegedExceptionAction<java.lang.String> __action = new PrivilegedExceptionAction<java.lang.String>() {
//                        public java.lang.String run() throws Exception {
//                            java.lang.String __result = __remote.getCountryName(arg0, arg1);
//                            return __result;
//                        }
//                    };
//
//                    return __jwsHelper.invoke(__action);
//
//                } catch(javax.ejb.EJBException ex) {
//                    if (((Throwable)ex).getCause() instanceof java.rmi.NoSuchObjectException) {
//                        __jwsHelper.clearPool();
//                    }
//                    else {
//                        throw ex;
//                    } 
//                } finally {
//                    __jwsHelper.close();
//                }
//
//            }
            return null;
        }

//        @WebMethod(action="http://www.openuri.org/getLastName")
//        @WebResult(name="return")
//        public java.lang.String getLastName( @WebParam(name="arg0") final com.bea.wli.sb.transports.ejb.test.xbean.MyAddress arg0) throws Exception
//        {
//            return null;
//        }
    }
