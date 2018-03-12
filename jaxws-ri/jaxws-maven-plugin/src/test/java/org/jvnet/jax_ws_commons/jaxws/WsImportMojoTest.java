/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jvnet.jax_ws_commons.jaxws;

import java.io.IOException;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Lukas Jungmann
 */
public class WsImportMojoTest {
    
    @Test
    public void testGetActiveHttpProxy() throws IOException, XmlPullParserException {
        SettingsXpp3Reader r = new SettingsXpp3Reader();
        Settings s = r.read(WsImportMojoTest.class.getResourceAsStream("proxy1.xml"));
        String proxyString = WsImportMojo.getActiveHttpProxy(s);
        Assert.assertEquals(proxyString, "proxyActive:8099");
        
        s = r.read(WsImportMojoTest.class.getResourceAsStream("proxy2.xml"));
        proxyString = WsImportMojo.getActiveHttpProxy(s);
        Assert.assertNull(proxyString, proxyString);

        s = r.read(WsImportMojoTest.class.getResourceAsStream("proxy3.xml"));
        proxyString = WsImportMojo.getActiveHttpProxy(s);
        Assert.assertEquals(proxyString, "proxyuser:proxypwd@proxy1-auth:8080");

        s = r.read(WsImportMojoTest.class.getResourceAsStream("proxy4.xml"));
        proxyString = WsImportMojo.getActiveHttpProxy(s);
        Assert.assertEquals(proxyString, "proxyuser2@proxy1-auth2:7777");
    }
}
