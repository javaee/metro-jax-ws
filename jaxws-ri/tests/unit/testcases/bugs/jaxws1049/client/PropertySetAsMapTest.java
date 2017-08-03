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

package bugs.jaxws1049.client;

import com.sun.xml.ws.api.PropertySet;
import junit.framework.TestCase;

import java.util.Map;

import static javax.xml.ws.BindingProvider.SOAPACTION_URI_PROPERTY;

/**
 * Test for new implementation of {@link com.sun.xml.ws.api.PropertySet#asMap()} ()} - it should be used instead of the
 * old implementation {@link com.sun.xml.ws.api.PropertySet#createMapView()} which is read only ...
 *
 * @author Miroslav Kos (miroslav.kos at oracle.com)
 */
public class PropertySetAsMapTest extends TestCase {

    public void test1NonExtensible() {

        MyPropertySet ctx = new MyPropertySet();
        Map<String, Object> map = ctx.asMap();

        assertSOAPActionCorrect(ctx, map, null);

        ctx.setSoapAction("customAction");
        assertSOAPActionCorrect(ctx, map, "customAction");

        map.put(SOAPACTION_URI_PROPERTY, "ANOTHERAction");
        assertSOAPActionCorrect(ctx, map, "ANOTHERAction");

        map.keySet(); // shouldn't change anything
        assertSOAPActionCorrect(ctx, map, "ANOTHERAction");

        map.put(SOAPACTION_URI_PROPERTY, null);
        assertSOAPActionCorrect(ctx, map, null);

        ctx.setSoapAction("YET ANOTHER ONE");
        assertSOAPActionCorrect(ctx, map, "YET ANOTHER ONE");

        try {
            map.put("unknownProperty", "Anything");
        } catch (IllegalStateException e) {
            // ok
        }

        // reading not existing property is ok(?)
        try {
            map.get("unknownProperty");
        } catch (IllegalStateException e) {
            // ok
        }
    }

    public void testExtensible() {

        MyExtensiblePropertySet ctx = new MyExtensiblePropertySet();
        Map<String, Object> map = ctx.asMap();

        assertSOAPActionCorrect(ctx, map, null);

        ctx.setSoapAction("customAction");
        assertSOAPActionCorrect(ctx, map, "customAction");

        map.put(SOAPACTION_URI_PROPERTY, "ANOTHERAction");
        assertSOAPActionCorrect(ctx, map, "ANOTHERAction");

        map.keySet(); // shouldn't change anything
        assertSOAPActionCorrect(ctx, map, "ANOTHERAction");

        map.put(SOAPACTION_URI_PROPERTY, null);
        assertSOAPActionCorrect(ctx, map, null);

        ctx.setSoapAction("YET ANOTHER ONE");
        assertSOAPActionCorrect(ctx, map, "YET ANOTHER ONE");

        map.put("unknownProperty", "Anything");
        assertSame(map.get("unknownProperty"), "Anything");
    }

    private void assertSOAPActionCorrect(MyPropertySet ctx, Map<String, Object> map, Object expected) {
        assertSame("Incorrect SOAPAction got via strongly typed getter", expected, ctx.getSoapAction());
        assertSame("Incorrect SOAPAction got via PropertySet.asMap()", expected, map.get(SOAPACTION_URI_PROPERTY));
    }

    private void assertSOAPActionCorrect(MyExtensiblePropertySet ctx, Map<String, Object> map, Object expected) {
        assertSame("Incorrect SOAPAction got via strongly typed getter", expected, ctx.getSoapAction());
        assertSame("Incorrect SOAPAction got via PropertySet.asMap()", expected, map.get(SOAPACTION_URI_PROPERTY));
    }

    class MyPropertySet extends PropertySet {

        @Property(SOAPACTION_URI_PROPERTY)
        private String soapAction;

        @Override
        protected PropertyMap getPropertyMap() {
            return parse(MyPropertySet.class);
        }

        public String getSoapAction() {
            return soapAction;
        }

        public void setSoapAction(String soapAction) {
            this.soapAction = soapAction;
        }
    }

    class MyExtensiblePropertySet extends PropertySet {

        @Property(SOAPACTION_URI_PROPERTY)
        private String soapAction;

        @Override
        protected PropertyMap getPropertyMap() {
            return parse(MyExtensiblePropertySet.class);
        }

        public String getSoapAction() {
            return soapAction;
        }

        public void setSoapAction(String soapAction) {
            this.soapAction = soapAction;
        }

        @Override
        protected boolean mapAllowsAdditionalProperties() {
            return true;
        }
    }

}
