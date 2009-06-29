/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.xml.ws.model;

import junit.framework.TestCase;

import java.util.List;
import java.util.HashMap;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * VM signature test for wrapper bean fields
 * 
 * @author Jitendra Kotamraju
 */
public class FieldSignatureTest<K,V> extends TestCase {

    public void test() throws Exception {
        assertEquals("B", FieldSignature.vms(byte.class));
        assertEquals("[B", FieldSignature.vms(byte[].class));
        assertEquals("Ljava/lang/String;", FieldSignature.vms(String.class));
        assertEquals("[Ljava/lang/Object;", FieldSignature.vms((new Object[3]).getClass()));
        assertEquals("[[[[[[[I", FieldSignature.vms((new int[3][4][5][6][7][8][9]).getClass()));
    }

    public List<List<String>[]> type1;
    public void test1() throws Exception {
        Field f = FieldSignatureTest.class.getField("type1");
        assertEquals("Ljava/util/List<[Ljava/util/List<Ljava/lang/String;>;>;", FieldSignature.vms(f.getGenericType()));
    }

    public List<?> type2;
    public void test2() throws Exception { 
        Field f = FieldSignatureTest.class.getField("type2");
        assertEquals("Ljava/util/List<*>;", FieldSignature.vms(f.getGenericType()));
    }

    public List<? super Integer> type3;
    public void test3() throws Exception {
        Field f = FieldSignatureTest.class.getField("type3");
        assertEquals("Ljava/util/List<-Ljava/lang/Integer;>;", FieldSignature.vms(f.getGenericType()));
    }

    public List<? extends Number> type4;
    public void test4() throws Exception {
        Field f = FieldSignatureTest.class.getField("type4");
        assertEquals("Ljava/util/List<+Ljava/lang/Number;>;", FieldSignature.vms(f.getGenericType()));
    }

    public byte[] type5;
    public void test5() throws Exception {
        Field f = FieldSignatureTest.class.getField("type5");
        assertEquals("[B", FieldSignature.vms(f.getGenericType()));
    }

    // Reflection API gives "GenericArrayType" for byte[] in this method
    // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5041784
    public void mytest(byte[] a, List<String> list) {
    }
    public void test6() throws Exception {
        Method[] ms = FieldSignatureTest.class.getMethods();
        Method m = FieldSignatureTest.class.getMethod("mytest", byte[].class, List.class);
        assertEquals("[B", FieldSignature.vms(m.getGenericParameterTypes()[0]));
    }

// While creating wrapper bean fields, it doesn't create with TypeVariables
// Otherwise, the type variable need to be declared in the wrapper bean class
//
//    public HashMap<K,V> type5;
//    public void test5() throws Exception {
//        Field f = FieldSignatureTest.class.getField("type5");
//        assertEquals("Ljava/util/HashMap<TK;TV;>;", FieldSignature.vms(f.getGenericType()));
//    }

}
