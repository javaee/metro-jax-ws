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

package com.sun.xml.ws.cts.dl_swa;

import java.awt.Image;

import javax.activation.DataHandler;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;

import javax.xml.transform.Source;
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import javax.xml.ws.Holder;

@WebService(targetNamespace="http://SwaTestService.org/wsdl", name="SwaTest1")
@SOAPBinding(parameterStyle=ParameterStyle.BARE, style=Style.DOCUMENT)
public interface SwaTest1
{
  @WebMethod
  @Action(input="http://SwaTestService.org/wsdl/SwaTest1/getMultipleAttachments/Request", 
    output="http://SwaTestService.org/wsdl/SwaTest1/getMultipleAttachments/Response")
  public void getMultipleAttachments(@WebParam(targetNamespace="http://SwaTestService.org/xsd", 
      partName="request", name="InputRequestGet")
    InputRequestGet request, @WebParam(targetNamespace="http://SwaTestService.org/xsd", 
      mode=Mode.OUT, partName="response", name="OutputResponse")
    Holder<OutputResponse> response, @WebParam(targetNamespace="", mode=Mode.OUT, 
      partName="attach1", name="attach1")
    Holder<DataHandler> attach1, @WebParam(targetNamespace="", mode=Mode.OUT, 
      partName="attach2", name="attach2")
    Holder<DataHandler> attach2);

  @WebMethod
  @Action(input="http://SwaTestService.org/wsdl/SwaTest1/putMultipleAttachments/Request", 
    output="http://SwaTestService.org/wsdl/SwaTest1/putMultipleAttachments/Response")
  @WebResult(targetNamespace="http://SwaTestService.org/xsd", partName="response", 
    name="OutputResponseString")
  public OutputResponseString putMultipleAttachments(@WebParam(targetNamespace="http://SwaTestService.org/xsd", 
      partName="request", name="InputRequestPut")
    InputRequestPut request, @WebParam(targetNamespace="", partName="attach1", 
      name="attach1")
    DataHandler attach1, @WebParam(targetNamespace="", partName="attach2", 
      name="attach2")
    DataHandler attach2);

  @WebMethod
  @Action(input="http://SwaTestService.org/wsdl/SwaTest1/echoMultipleAttachments/Request", 
    output="http://SwaTestService.org/wsdl/SwaTest1/echoMultipleAttachments/Response")
  @WebResult(targetNamespace="http://SwaTestService.org/xsd", partName="response", 
    name="OutputResponse")
  public OutputResponse echoMultipleAttachments(@WebParam(targetNamespace="http://SwaTestService.org/xsd", 
      partName="request", name="InputRequest")
    InputRequest request, @WebParam(targetNamespace="", mode=Mode.INOUT, 
      partName="attach1", name="attach1")
    Holder<DataHandler> attach1, @WebParam(targetNamespace="", mode=Mode.INOUT, 
      partName="attach2", name="attach2")
    Holder<DataHandler> attach2);

  @WebMethod
  @Action(input="http://SwaTestService.org/wsdl/SwaTest1/echoNoAttachments/Request", 
    output="http://SwaTestService.org/wsdl/SwaTest1/echoNoAttachments/Response")
  @WebResult(targetNamespace="http://SwaTestService.org/xsd", partName="response", 
    name="OutputResponseString")
  public OutputResponseString echoNoAttachments(@WebParam(targetNamespace="http://SwaTestService.org/xsd", 
      partName="request", name="InputRequestString")
    InputRequestString request);

  @WebMethod
  @Action(input="http://SwaTestService.org/wsdl/SwaTest1/echoAllAttachmentTypes/Request", 
    output="http://SwaTestService.org/wsdl/SwaTest1/echoAllAttachmentTypes/Response")
  @WebResult(targetNamespace="http://SwaTestService.org/xsd", partName="response", 
    name="OutputResponseAll")
  public OutputResponseAll echoAllAttachmentTypes(@WebParam(targetNamespace="http://SwaTestService.org/xsd", 
      partName="request", name="VoidRequest")
    VoidRequest request, @WebParam(targetNamespace="", mode=Mode.INOUT, 
      partName="attach1", name="attach1")
    Holder<DataHandler> attach1, @WebParam(targetNamespace="", mode=Mode.INOUT, 
      partName="attach2", name="attach2")
    Holder<DataHandler> attach2, @WebParam(targetNamespace="", mode=Mode.INOUT, 
      partName="attach3", name="attach3")
    Holder<Source> attach3, @WebParam(targetNamespace="", mode=Mode.INOUT, 
      partName="attach4", name="attach4")
    Holder<Image> attach4, @WebParam(targetNamespace="", mode=Mode.INOUT, 
      partName="attach5", name="attach5")
    Holder<Image> attach5);

  @WebMethod
  @Action(input="http://SwaTestService.org/wsdl/SwaTest1/echoAttachmentsAndThrowAFault/Request", fault =
      { @FaultAction(value="http://SwaTestService.org/wsdl/SwaTest1/echoAttachmentsAndThrowAFault/Fault/MyFault", 
          className = MyFault.class)
        } , output="http://SwaTestService.org/wsdl/SwaTest1/echoAttachmentsAndThrowAFault/Response")
  @WebResult(targetNamespace="http://SwaTestService.org/xsd", partName="response", 
    name="OutputResponse")
  public OutputResponse echoAttachmentsAndThrowAFault(@WebParam(targetNamespace="http://SwaTestService.org/xsd", 
      partName="request", name="InputRequestThrowAFault")
    InputRequestThrowAFault request, @WebParam(targetNamespace="", mode=Mode.INOUT, 
      partName="attach1", name="attach1")
    Holder<DataHandler> attach1, @WebParam(targetNamespace="", mode=Mode.INOUT, 
      partName="attach2", name="attach2")
    Holder<DataHandler> attach2)
    throws MyFault;

  @WebMethod
  @Action(input="http://SwaTestService.org/wsdl/SwaTest1/echoAttachmentsWithHeader/Request", fault =
      { @FaultAction(value="http://SwaTestService.org/wsdl/SwaTest1/echoAttachmentsWithHeader/Fault/MyFault", 
          className = MyFault.class)
        } , output="http://SwaTestService.org/wsdl/SwaTest1/echoAttachmentsWithHeader/Response")
  @WebResult(targetNamespace="http://SwaTestService.org/xsd", partName="response", 
    name="OutputResponse")
  public OutputResponse echoAttachmentsWithHeader(@WebParam(targetNamespace="http://SwaTestService.org/xsd", 
      partName="request", name="InputRequestWithHeader")
    InputRequestWithHeader request, @WebParam(targetNamespace="http://SwaTestService.org/xsd", 
      header=true, partName="header", name="MyHeader")
    MyHeader header, @WebParam(targetNamespace="", mode=Mode.INOUT, 
      partName="attach1", name="attach1")
    Holder<DataHandler> attach1, @WebParam(targetNamespace="", mode=Mode.INOUT, 
      partName="attach2", name="attach2")
    Holder<DataHandler> attach2)
    throws MyFault;
  

  /**
   * EnableMIMEContent = false.
   */
  @WebMethod
  @Action(input="http://example.org/mime/Hello/echoData/Request", output="http://example.org/mime/Hello/echoData/Response")
  @SOAPBinding(parameterStyle=ParameterStyle.BARE)
  public void echoData(@WebParam(targetNamespace="http://example.org/mime/data", 
      partName="body", name="body")
    String body, @WebParam(targetNamespace="", mode=Mode.INOUT, partName="data", 
      name="data")
    Holder<byte[]> data);
}
