/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
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

package benchmark.rpclit.server;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.jws.WebService;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author JAX-RPC RI Development Team
 */
@WebService(endpointInterface="benchmark.rpclit.server.EchoPortType")
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

	public benchmark.rpclit.server.Enum echoEnum(benchmark.rpclit.server.Enum param) {
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
