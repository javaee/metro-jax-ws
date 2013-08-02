/*
 * $Id: EchoPortTypeImpl.java,v 1.1 2007-08-31 22:19:29 kohsuke Exp $
 */

package benchmark.rpclit;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.jws.WebService;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author JAX-RPC RI Development Team
 */
@WebService(endpointInterface="benchmark.rpclit.EchoPortType")
public class EchoPortTypeImpl implements EchoPortType {
	public void echoVoid() {
	}

	public boolean echoBoolean(boolean param) {
		return param;
	}

	public int echoInteger(int param) {
		return param;
	}

	public float echoFloat(float param) {
		return param;
	}

	public String echoString(String inputString) {
		return inputString;
	}

	public byte[] echoBase64(byte[] param) {
		return param;
	}

	public XMLGregorianCalendar echoDate(XMLGregorianCalendar param) {
		return param;
	}

	public Enum echoEnum(Enum param) {
		return param;
	}

	public BigDecimal echoDecimal(BigDecimal param) {
		return param;
	}

	public ComplexType echoComplexType(ComplexType param) {
		return param;
	}

	public NestedComplexType echoNestedComplexType(NestedComplexType param) {
		return param;
	}

	public IntegerArray echoIntegerArray(IntegerArray param) {
		return param;
	}

	public FloatArray echoFloatArray(FloatArray param) {
		return param;
	}

	public StringArray echoStringArray(StringArray param) {
		return param;
	}

	public ComplexTypeArray echoComplexTypeArray(ComplexTypeArray param) {
		return param;
	}
}
