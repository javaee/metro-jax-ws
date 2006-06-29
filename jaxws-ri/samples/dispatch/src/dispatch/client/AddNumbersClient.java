/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package dispatch.client;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Dispatch;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

public class AddNumbersClient {

    private static String request = "<addNumbers xmlns=\"http://duke.example\"><arg0>10</arg0><arg1>20</arg1></addNumbers>";

    private static String smRequest = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><addNumbers xmlns=\"http://duke.example\"><arg0>10</arg0><arg1>20</arg1></addNumbers></soapenv:Body></soapenv:Envelope>";

    private static final QName serviceQName = new QName("http://duke.example", "AddNumbersService");
    private static final QName portQName = new QName("http://duke.example", "AddNumbersPort");
    private static String endpointAddress = "http://localhost:8080/jaxws-dispatch/addnumbers";
    private static Service service;

    public static void main(String[] args) {

        AddNumbersClient client = new AddNumbersClient();

        String soapBindingURI = SOAPBinding.SOAP11HTTP_BINDING;
        service = new AddNumbersService();

        try {
            //dispatch using Source
            client.invokeAddNumbers(request, Service.Mode.PAYLOAD);

            //dispatch using JAXBContext
            client.invokeAddNumbers();

            //dispatch using SOAPMessage
            client.invokeAddNumbers(smRequest);

            client.invokeAsyncPollAddNumbers(request, Service.Mode.PAYLOAD);
            client.invokeAsyncPollAddNumbers();
            client.invokeAsyncPollAddNumbers(smRequest);

            client.invokeAsyncCallbackAddNumbers(request, Service.Mode.PAYLOAD);
            client.invokeAsyncCallbackAddNumbers();
            client.invokeAsyncCallbackAddNumbers(smRequest);

        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (ProtocolException jex) {
            jex.printStackTrace();
        }
    }


    ////invoke
    private void invokeAddNumbers(String request, Service.Mode mode) throws RemoteException {
        Dispatch<Source> sourceDispatch = null;
        sourceDispatch = service.createDispatch(portQName, Source.class, mode);
        System.out.println("\nInvoking xml request: " + request);
        Source result = sourceDispatch.invoke(new StreamSource(new StringReader(request)));

        InputStream in = null;
        OutputStream out = null;

        String xmlResult = sourceToXMLString(result);
        System.out.println("Received xml response: " + xmlResult);
    }

    private String sourceToXMLString(Source result) {

        String xmlResult = null;
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            OutputStream out = new ByteArrayOutputStream();
            StreamResult streamResult = new StreamResult();
            streamResult.setOutputStream(out);
            transformer.transform(result, streamResult);
            xmlResult = streamResult.getOutputStream().toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return xmlResult;
    }

    private void invokeAddNumbers() throws RemoteException {
        Dispatch jaxbDispatch = null;
        JAXBContext jaxbContext = createJAXBContext();
        jaxbDispatch = service.createDispatch(portQName, jaxbContext, Service.Mode.PAYLOAD);

        ObjectFactory factory = new ObjectFactory();
        AddNumbers numbers = new AddNumbers();
        int number1 = 10;
        int number2 = 20;
        numbers.setArg0(number1);
        numbers.setArg1(number2);

        JAXBElement<AddNumbers> addNumbers = factory.createAddNumbers(numbers);

        System.out.printf("\nInvoking addNumbers(%d, %d)\n", number1, number2);
        JAXBElement<AddNumbersResponse> response =
            (JAXBElement<AddNumbersResponse>) jaxbDispatch.invoke(addNumbers);

        AddNumbersResponse result = response.getValue();
        System.out.printf("The result of adding %d and %d is %d.\n", number1, number2, result.getReturn());

    }

    private void invokeAddNumbers(String msgString) throws RemoteException {

        SOAPMessage message = null;
        try {
            MessageFactory factory = MessageFactory.newInstance();
            message = factory.createMessage();
            message.getSOAPPart().setContent((Source) new StreamSource(new StringReader(msgString)));
            message.saveChanges();
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        Dispatch<SOAPMessage> smDispatch = null;
        smDispatch = service.createDispatch(portQName, SOAPMessage.class, Service.Mode.MESSAGE);
        System.out.println("\nInvoking message: " + msgString);
        SOAPMessage response = smDispatch.invoke(message);
        String xmlString = null;
        try {
            Source result = response.getSOAPPart().getContent();
            xmlString = sourceToXMLString(result);
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        System.out.println("Received response: " + xmlString);

    }
    //end invoke


    //invoke async poll
    private void invokeAsyncPollAddNumbers(String request, Service.Mode mode) throws RemoteException {
        Dispatch<Source> sourceDispatch = null;
        sourceDispatch = service.createDispatch(portQName, Source.class, mode);

        System.out.println("\nInvoking async poll xml request: " + request);
        Response<Source> response = sourceDispatch.invokeAsync(new StreamSource(new StringReader(request)));

        while (!response.isDone()) {
            //go do some work
        }

        String xmlResult = null;
        try {
            Source result = response.get();
            xmlResult = sourceToXMLString(result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("Received xml response: " + xmlResult);
    }

    private void invokeAsyncPollAddNumbers() throws RemoteException {
        Dispatch jaxbDispatch = null;
        JAXBContext jaxbContext = createJAXBContext();
        jaxbDispatch = service.createDispatch(portQName, jaxbContext, Service.Mode.PAYLOAD);

        ObjectFactory factory = new ObjectFactory();
        AddNumbers numbers = new AddNumbers();
        int number1 = 10;
        int number2 = 20;
        numbers.setArg0(number1);
        numbers.setArg1(number2);

        JAXBElement<AddNumbers> addNumbers = factory.createAddNumbers(numbers);

        System.out.printf("\nInvoking async poll addNumbers(%d, %d)\n", number1, number2);
        Response<JAXBElement<AddNumbersResponse>> response = jaxbDispatch.invokeAsync(addNumbers);

        while (!response.isDone()) {
            //go do some work
        }

        AddNumbersResponse result = null;
        try {
            JAXBElement<AddNumbersResponse> jaxbResponse = response.get();
            result = jaxbResponse.getValue();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.printf("The result of adding %d and %d is %d.\n", number1, number2, result.getReturn());
    }

    private void invokeAsyncPollAddNumbers(String msgString) throws RemoteException {

        SOAPMessage message = null;
        try {
            MessageFactory factory = MessageFactory.newInstance();
            message = factory.createMessage();
            message.getSOAPPart().setContent((Source) new StreamSource(new StringReader(msgString)));
            message.saveChanges();
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        Dispatch<SOAPMessage> smDispatch = null;
        smDispatch = service.createDispatch(portQName, SOAPMessage.class, Service.Mode.MESSAGE);

        System.out.println("\nInvoking async poll message: " + msgString);
        Response<SOAPMessage> response = smDispatch.invokeAsync(message);

        while (!response.isDone()) {
            //go do some work
        }
        String xmlString = null;

        try {
            SOAPMessage result = response.get();
            Source sourceResult = (Source) result.getSOAPPart().getContent();
            xmlString = sourceToXMLString(sourceResult);
        } catch (SOAPException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("Received response: " + xmlString);
    }

    //end invoke async poll
    private void invokeAsyncCallbackAddNumbers(String request, Service.Mode mode) throws RemoteException {
        Dispatch sourceDispatch = null;
        sourceDispatch = service.createDispatch(portQName, Source.class, mode);

        System.out.println("\nInvoking async calback xml request: " + request);
        DispatchAsyncHandler handler = new DispatchAsyncHandler();
        Future<?> response = sourceDispatch.invokeAsync(new StreamSource(new StringReader(request)), new DispatchAsyncHandler());

        //go off and do something else
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (response.isDone()) {
            if (handler.isFailure()) {
                Throwable failure = handler.getFailure();
                System.out.println("Failure in DispatchAsyncHandler " + failure.getMessage());
            } else
                System.out.println("Success processing result!");
        }
    }

    private void invokeAsyncCallbackAddNumbers() {
        Dispatch jaxbDispatch = null;
        JAXBContext jaxbContext = createJAXBContext();
        jaxbDispatch = service.createDispatch(portQName, jaxbContext, Service.Mode.PAYLOAD);

        ObjectFactory factory = new ObjectFactory();
        AddNumbers numbers = new AddNumbers();
        int number1 = 10;
        int number2 = 20;
        numbers.setArg0(number1);
        numbers.setArg1(number2);

        JAXBElement<AddNumbers> addNumbers = factory.createAddNumbers(numbers);

        System.out.printf("\nInvoking async callback addNumbers(%d, %d)\n", number1, number2);
        DispatchAsyncHandler handler = new DispatchAsyncHandler();
        Future<?> response = jaxbDispatch.invokeAsync(addNumbers, handler);

        //go off and do other work
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (response.isDone()) {
            if (handler.isFailure()) {
                Throwable failure = handler.getFailure();
                System.out.println("Failure in DispatchAsyncHandler " + failure.getMessage());
            } else
                System.out.println("Success processing result!");
        }
    }

    private void invokeAsyncCallbackAddNumbers(String msgString) throws RemoteException {

        SOAPMessage message = null;
        try {
            MessageFactory factory = MessageFactory.newInstance();
            message = factory.createMessage();
            message.getSOAPPart().setContent((Source) new StreamSource(new StringReader(msgString)));
            message.saveChanges();
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        Dispatch smDispatch = null;
        smDispatch = service.createDispatch(portQName, SOAPMessage.class, Service.Mode.MESSAGE);

        System.out.println("\nInvoking async callback message: " + msgString);
        DispatchAsyncHandler handler = new DispatchAsyncHandler();
        Future<?> response = smDispatch.invokeAsync(message, handler);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (response.isDone()) {
            if (handler.isFailure()) {
                Throwable failure = handler.getFailure();
                System.out.println("Failure in DispatchAsyncHandler " + failure.getMessage());
            } else
                System.out.println("Success processing result!");
        }
    }


    private static javax.xml.bind.JAXBContext createJAXBContext() {
        try {
            return javax.xml.bind.JAXBContext.newInstance(dispatch.client.ObjectFactory.class);
        } catch (javax.xml.bind.JAXBException e) {
            throw new WebServiceException(e.getMessage(), e);
        }
    }

    class DispatchAsyncHandler implements AsyncHandler {
        boolean failed = false;
        Throwable failure = null;

        public void handleResponse(Response response) {
            try {
                Object result = response.get();
                failed = result instanceof Source ? handleSource((Source) result) :
                    result instanceof SOAPMessage ? handleSOAPMessage((SOAPMessage) result) :
                    handleJAXBObject((JAXBElement<AddNumbersResponse>) result);
            } catch (InterruptedException e) {
                failed = true;
                failure = e;
            } catch (ExecutionException e) {
                failed = true;
                failure = e;
            } catch (SOAPException e) {
                failed = true;
                failure = e;
            }
        }

        public boolean isFailure() {
            return failed;
        }

        public Throwable getFailure() {
            return failure.getCause();
        }

        boolean handleSource(Source source) {
            String xmlString = sourceToXMLString(source);
            //do something with the result
            return false;
        }

        boolean handleSOAPMessage(SOAPMessage msg) throws SOAPException {
            Source source = (Source) msg.getSOAPPart().getContent();
            String xmlString = sourceToXMLString(source);
            //do something with the result
            return false;
        }

        boolean handleJAXBObject(JAXBElement<AddNumbersResponse> obj) {
            AddNumbersResponse response = obj.getValue();
            int result = response.getReturn();
            //do something with the result
            return false;
        }
    }

}
