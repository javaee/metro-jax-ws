/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.ws.wscompile.plugin.at_generated;

import com.sun.codemodel.*;
import com.sun.tools.ws.ToolVersion;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import com.sun.tools.ws.wscompile.Plugin;
import com.sun.tools.ws.wscompile.WsimportOptions;
import com.sun.tools.ws.wscompile.WsimportTool;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import org.xml.sax.SAXException;

/**
 * {@link Plugin} that marks the generated code by using JSR-250's '@Generated'.
 * It is based on a similar plugin in JAXB RI.
 *
 * @author Lukas Jungmann
 * @since 2.2.6
 */
public final class PluginImpl extends Plugin {

    private JClass annotation;

    // cache the timestamp so that all the @Generated annotations match
    private String date = null;

    @Override
    public String getOptionName() {
        return "mark-generated";
    }

    @Override
    public String getUsage() {
        return "  -mark-generated    :  mark the generated code as @javax.annotation.Generated";
    }

    @Override
    public boolean run(Model model, WsimportOptions wo, ErrorReceiver er) throws SAXException {
        JCodeModel cm = wo.getCodeModel();
        // we want this to work without requiring JSR-250 jar.
        annotation = cm.ref("javax.annotation.Generated");

        for (Iterator<JPackage> i = cm.packages(); i.hasNext();) {
            for (Iterator<JDefinedClass> j = i.next().classes(); j.hasNext();) {
                annotate(j.next());
            }
        }
        
        return true;
    }

    private void annotate(JAnnotatable m) {
        m.annotate(annotation)
                .param("value", WsimportTool.class.getName())
                .param("date", getISO8601Date())
                .param("comments", ToolVersion.VERSION.BUILD_VERSION);
    }

    /**
     * calculate the date value in ISO8601 format for the @Generated annotation
     * @return the date value
     */
    private String getISO8601Date() {
        if(date==null) {
            StringBuilder tstamp = new StringBuilder();
            tstamp.append((new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ")).format(new Date()));
            // hack to get ISO 8601 style timezone - is there a better way that doesn't require
            // a bunch of timezone offset calculations?
            tstamp.insert(tstamp.length()-2, ':');
            date = tstamp.toString();
        }
        return date;
    }
}
