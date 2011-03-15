package com.sun.xml.ws.test;

import static javax.jws.soap.SOAPBinding.Style.RPC;
import static javax.jws.soap.SOAPBinding.Use.LITERAL;

import java.util.List;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(targetNamespace="http://www.oracle.com/webservices/tests")
@SOAPBinding(style = RPC, use = LITERAL)
public class CollectionMapImpl {
	public List<String> echoListOfString(List<String> l) {
		return l;
	}
}
