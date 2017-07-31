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

package com.sun.xml.ws.spi.db;

import java.lang.reflect.Type;

//TODO SOAPVersion WebServiceFeatureList
import com.sun.xml.bind.util.Which;

//TODO Packet AbstractMessageImpl
import com.sun.xml.bind.marshaller.SAX2DOMEx;

//TODO DOMHeader DOMMessage SAAJMessage StatefulInstanceResolver
import com.sun.xml.bind.unmarshaller.DOMScanner;

//TODO ExceptionBean
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

//TODO AbstractWrapperBeanGenerator
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.RuntimeInlineAnnotationReader;
import com.sun.xml.bind.v2.model.nav.Navigator;

//TODO WSDLGenerator
import static com.sun.xml.bind.v2.schemagen.Util.*;

import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.bind.v2.model.nav.Navigator;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
/**
 * BindingHelper
 *
 * @author shih-chang.chen@oracle.com
 */
public class BindingHelper {
    /**
     * Computes a Java identifier from a local name.
     *
     * <p>
     * This method faithfully implements the name mangling rule as specified in the JAXB spec.
     *
     * <p>
     * In JAXB, a collision with a Java reserved word (such as "return") never happens.
     * Accordingly, this method may return an identifier that collides with reserved words.
     *
     * <p>
     * Use {@code JJavaName.isJavaIdentifier(String)} to check for such collision.
     *
     * @return
     *      Typically, this method returns "nameLikeThis".
     */
    public static @NotNull String mangleNameToVariableName(@NotNull String localName) {
        return NameConverter.standard.toVariableName(localName);
    }

    /**
     * Computes a Java class name from a local name.
     *
     * <p>
     * This method faithfully implements the name mangling rule as specified in the JAXB spec.
     *
     * @return
     *      Typically, this method returns "NameLikeThis".
     */
    public static @NotNull String mangleNameToClassName(@NotNull String localName) {
        return NameConverter.standard.toClassName(localName);
    }

    /**
     * Computes a Java class name from a local name.
     *
     * <p>
     * This method faithfully implements the name mangling rule as specified in the JAXB spec.
     * This method works like {@link #mangleNameToClassName(String)} except that it looks
     * for "getClass" and returns something else.
     *
     * @return
     *      Typically, this method returns "NameLikeThis".
     */
    public static @NotNull String mangleNameToPropertyName(@NotNull String localName) {
        return NameConverter.standard.toPropertyName(localName);
    }

    /**
     * Gets the parameterization of the given base type.
     *
     * <p>
     * For example, given the following
     * <pre>{@code
     * interface Foo<T> extends List<List<T>> {}
     * interface Bar extends Foo<String> {}
     * }</pre>
     * This method works like this:
     * <pre>{@code
     * getBaseClass( Bar, List ) = List<List<String>
     * getBaseClass( Bar, Foo  ) = Foo<String>
     * getBaseClass( Foo<? extends Number>, Collection ) = Collection<List<? extends Number>>
     * getBaseClass( ArrayList<? extends BigInteger>, List ) = List<? extends BigInteger>
     * }</pre>
     *
     * @param type
     *      The type that derives from {@code baseType}
     * @param baseType
     *      The class whose parameterization we are interested in.
     * @return
     *      The use of {@code baseType} in {@code type}.
     *      or null if the type is not assignable to the base type.
     * @since 2.0 FCS
     */
    public static @Nullable Type getBaseType(@NotNull Type type, @NotNull Class baseType) {
        return Utils.REFLECTION_NAVIGATOR.getBaseClass(type,baseType);
    }
    
    public static <T> Class<T> erasure(Type t) {
        return (Class<T>) Utils.REFLECTION_NAVIGATOR.erasure(t);
    }
}
