/*
 * $Id: EchoPortTypeImpl.java,v 1.1 2007-08-31 22:19:28 kohsuke Exp $
 */
package benchmark.doclit;

import java.math.BigDecimal;
import java.util.List;

import javax.jws.WebService;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author JAX-RPC RI Development Team
 */
@WebService(endpointInterface="benchmark.doclit.EchoPortType")
public class EchoPortTypeImpl implements EchoPortType {
	public void echoVoid() {
	}

	public boolean echoBoolean(boolean param) {
		return param;
	}

	public String echoString(String inputString) {
		return inputString;
	}

	public List<String> echoStringArray(List<String> param) {
		return param;
	}

	public int echoInteger(int param) {
		return param;
	}

	public List<Integer> echoIntegerArray(List<Integer> param) {
		return param;
	}

	public float echoFloat(float param) {
		return param;
	}

	public List<Float> echoFloatArray(List<Float> param) {
		return param;
	}

	public ComplexType echoComplexType(ComplexType param) {
		return param;
	}

	public List<ComplexType> echoComplexTypeArray(List<ComplexType> param) {
		return param;
	}

	public byte[] echoBase64(byte[] param) {
		return param;
	}

	public XMLGregorianCalendar echoDate(XMLGregorianCalendar param) {
		return param;
	}

	public BigDecimal echoDecimal(java.math.BigDecimal param) {
		return param;
	}

	public Enum echoEnum(Enum param) {
		return param;
	}

	public NestedComplexType echoNestedComplexType(NestedComplexType param) {
		return param;
	}
}
