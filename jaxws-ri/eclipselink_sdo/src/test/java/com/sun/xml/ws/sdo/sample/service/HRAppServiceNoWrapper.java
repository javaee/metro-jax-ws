/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.sdo.sample.service;

import java.math.BigInteger;

import java.util.List;

import com.sun.xml.ws.sdo.sample.service.types.*;
//import oracle.j2ee.ws.common.sdo.SchemaLocation;

@javax.jws.WebService(targetNamespace="http://sdo.sample.service/", name="HRAppService", wsdlLocation="build/client/data/sdo/HRAppService.wsdl")
@javax.jws.soap.SOAPBinding(parameterStyle=javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED,
                            style=javax.jws.soap.SOAPBinding.Style.DOCUMENT)
//@SchemaLocation(value = "data/sdo/HRAppService.wsdl")

public interface HRAppServiceNoWrapper {
  @javax.xml.ws.RequestWrapper(targetNamespace="http://sdo.sample.service/typesX/",
//                               className="com.sun.xml.ws.sdo.sample.service.types.CreateEmpElement", 
                               localName="createEmpElement")
  @javax.jws.WebMethod(action="http://sdo.sample.service//createEmp",
                       operationName="createEmp")
  @javax.xml.ws.ResponseWrapper(targetNamespace="http://sdo.sample.service/typesX/",
//                                className="com.sun.xml.ws.sdo.sample.service.types.CreateEmpResponseElement",
                                localName="createEmpResponseElement")
  @javax.jws.WebResult(name="emp")
  public Emp createEmp(@javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                           name="emp")
    Emp emp);

//  @javax.xml.ws.RequestWrapper(targetNamespace="http://sdo.sample.service/types/",
//                               className="com.sun.xml.ws.sdo.sample.service.types.DeleteEmpElement", localName="deleteEmpElement")
//  @javax.xml.ws.ResponseWrapper(targetNamespace="http://sdo.sample.service/types/",
//                                className="com.sun.xml.ws.sdo.sample.service.types.DeleteEmpResponseElement",
//                                localName="deleteEmpResponseElement")
  @javax.jws.WebMethod(action="http://sdo.sample.service//deleteEmp",
                       operationName="deleteEmp")
  public void deleteEmp(@javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                            name="emp")
    Emp emp);

  @javax.jws.WebMethod(action="http://sdo.sample.service//findClerks",
                       operationName="findClerks")
//  @javax.xml.ws.ResponseWrapper(targetNamespace="http://sdo.sample.service/types/",
//                                className="com.sun.xml.ws.sdo.sample.service.types.FindClerksResponseElement",
//                                localName="findClerksResponseElement")
//  @javax.xml.ws.RequestWrapper(targetNamespace="http://sdo.sample.service/types/",
//                               className="com.sun.xml.ws.sdo.sample.service.types.FindClerksElement", localName="findClerksElement")
  @javax.jws.WebResult(name="emp")
  public List<Emp> findClerks(@javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                                  name="findCriteria")
    FindCriteria findCriteria, @javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                                   name="findControl")
    FindControl findControl);

//  @javax.xml.ws.RequestWrapper(targetNamespace="http://sdo.sample.service/types/",
//                               className="com.sun.xml.ws.sdo.sample.service.types.FindDeptsElement", localName="findDeptsElement")
//  @javax.xml.ws.ResponseWrapper(targetNamespace="http://sdo.sample.service/types/",
//                                className="com.sun.xml.ws.sdo.sample.service.types.FindDeptsResponseElement",
//                                localName="findDeptsResponseElement")
  @javax.jws.WebMethod(action="http://sdo.sample.service//findDepts",
                       operationName="findDepts")
  @javax.jws.WebResult(name="dept")
  public List<Dept> findDepts(@javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                                  name="findCriteria")
    FindCriteria findCriteria, @javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                                   name="findControl")
    FindControl findControl);

  @javax.jws.WebMethod(action="http://sdo.sample.service//findEmps",
                       operationName="findEmps")
//  @javax.xml.ws.ResponseWrapper(targetNamespace="http://sdo.sample.service/types/",
//                                className="com.sun.xml.ws.sdo.sample.service.types.FindEmpsResponseElement",
//                                localName="findEmpsResponseElement")
//  @javax.xml.ws.RequestWrapper(targetNamespace="http://sdo.sample.service/types/",
//                               className="com.sun.xml.ws.sdo.sample.service.types.FindEmpsElement", localName="findEmpsElement")
  @javax.jws.WebResult(name="emp")
  public List<Emp> findEmps(@javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                                name="findCriteria")
    FindCriteria findCriteria, @javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                                   name="findControl")
    FindControl findControl);
//
//  @javax.xml.ws.RequestWrapper(targetNamespace="http://sdo.sample.service/types/",
//                               className="com.sun.xml.ws.sdo.sample.service.types.FindEmpsByJobElement", localName="findEmpsByJobElement")
  @javax.jws.WebMethod(action="http://sdo.sample.service//findEmpsByJob",
                       operationName="findEmpsByJob")
//  @javax.xml.ws.ResponseWrapper(targetNamespace="http://sdo.sample.service/types/",
//                                className="com.sun.xml.ws.sdo.sample.service.types.FindEmpsByJobResponseElement",
//                                localName="findEmpsByJobResponseElement")
  @javax.jws.WebResult(name="emp")
  public List<Emp> findEmpsByJob(@javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                                     name="findCriteria")
    FindCriteria findCriteria, @javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                                   name="job")
    String job, @javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN, name="findControl")
    FindControl findControl);

//  @javax.xml.ws.ResponseWrapper(targetNamespace="http://sdo.sample.service/types/",
//                                className="com.sun.xml.ws.sdo.sample.service.types.GetDeptResponseElement",
//                                localName="getDeptResponseElement")
//  @javax.xml.ws.RequestWrapper(targetNamespace="http://sdo.sample.service/types/",
//                               className="com.sun.xml.ws.sdo.sample.service.types.GetDeptElement", localName="getDeptElement")
  @javax.jws.WebMethod(action="http://sdo.sample.service//getDept",
                       operationName="getDept")
  @javax.jws.WebResult(name="dept")
  public Dept getDept(@javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                          name="deptno")
    BigInteger deptno);

//  @javax.xml.ws.RequestWrapper(targetNamespace="http://sdo.sample.service/types/",
//                               className="com.sun.xml.ws.sdo.sample.service.types.GetEmpElement", localName="getEmpElement")
//  @javax.xml.ws.ResponseWrapper(targetNamespace="http://sdo.sample.service/types/",
//                                className="com.sun.xml.ws.sdo.sample.service.types.GetEmpResponseElement", localName="getEmpResponseElement")
  @javax.jws.WebMethod(action="http://sdo.sample.service//getEmp",
                       operationName="getEmp")
  @javax.jws.WebResult(name="emp")
  public Emp getEmp(@javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                        name="empno")
    BigInteger empno);

//  @javax.xml.ws.ResponseWrapper(targetNamespace="http://sdo.sample.service/types/",
//                                className="com.sun.xml.ws.sdo.sample.service.types.GetManagerAndPeersResponseElement",
//                                localName="getManagerAndPeersResponseElement")
  @javax.jws.WebMethod(action="http://sdo.sample.service//getManagerAndPeers",
                       operationName="getManagerAndPeers")
//  @javax.xml.ws.RequestWrapper(targetNamespace="http://sdo.sample.service/types/",
//                               className="com.sun.xml.ws.sdo.sample.service.types.GetManagerAndPeersElement",
//                               localName="getManagerAndPeersElement")
  @javax.jws.WebResult(name="emp")
  public List<Emp> getManagerAndPeers(@javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                                          name="empno")
    BigInteger empno);

  @javax.jws.WebMethod(action="http://sdo.sample.service//getTotalComp",
                       operationName="getTotalComp")
//  @javax.xml.ws.ResponseWrapper(targetNamespace="http://sdo.sample.service/types/",
//                                className="com.sun.xml.ws.sdo.sample.service.types.GetTotalCompResponseElement",
//                                localName="getTotalCompResponseElement")
//  @javax.xml.ws.RequestWrapper(targetNamespace="http://sdo.sample.service/types/",
//                               className="com.sun.xml.ws.sdo.sample.service.types.GetTotalCompElement", localName="getTotalCompElement")
  @javax.jws.WebResult(name="result", targetNamespace="http://sdo.sample.service/types/")
  public java.math.BigDecimal getTotalComp(@javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                                name="empno", targetNamespace="http://sdo.sample.service/types/")
    BigInteger empno);

  @javax.jws.WebMethod(action="http://sdo.sample.service//mergeEmp",
                       operationName="mergeEmp")
//  @javax.xml.ws.ResponseWrapper(targetNamespace="http://sdo.sample.service/types/",
//                                className="com.sun.xml.ws.sdo.sample.service.types.MergeEmpResponseElement",
//                                localName="mergeEmpResponseElement")
//  @javax.xml.ws.RequestWrapper(targetNamespace="http://sdo.sample.service/types/",
//                               className="com.sun.xml.ws.sdo.sample.service.types.MergeEmpElement", localName="mergeEmpElement")
  @javax.jws.WebResult(name="emp")
  public Emp mergeEmp(@javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                          name="emp")
    Emp emp);

  @javax.jws.WebMethod(action="http://sdo.sample.service//processEmps",
                       operationName="processEmps")
//  @javax.xml.ws.ResponseWrapper(targetNamespace="http://sdo.sample.service/types/",
//                                className="com.sun.xml.ws.sdo.sample.service.types.ProcessEmpsResponseElement",
//                                localName="processEmpsResponseElement")
//  @javax.xml.ws.RequestWrapper(targetNamespace="http://sdo.sample.service/types/",
//                               className="com.sun.xml.ws.sdo.sample.service.types.ProcessEmpsElement", localName="processEmpsElement")
  @javax.jws.WebResult(name="emp")
  public List<Emp> processEmps(@javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                                   name="changeOperation")
    String changeOperation, @javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                                name="emp")
    List<Emp> emp, @javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                       name="processControl")
    ProcessControl processControl);

//  @javax.xml.ws.RequestWrapper(targetNamespace="http://sdo.sample.service/types/",
//                               className="com.sun.xml.ws.sdo.sample.service.types.UpdateEmpElement", localName="updateEmpElement")
//  @javax.xml.ws.ResponseWrapper(targetNamespace="http://sdo.sample.service/types/",
//                                className="com.sun.xml.ws.sdo.sample.service.types.UpdateEmpResponseElement",
//                                localName="updateEmpResponseElement")
  @javax.jws.WebMethod(action="http://sdo.sample.service//updateEmp",
                       operationName="updateEmp")
  @javax.jws.WebResult(name="emp")
  public Emp updateEmp(@javax.jws.WebParam(mode=javax.jws.WebParam.Mode.IN,
                                           name="emp")
    Emp emp);
}
