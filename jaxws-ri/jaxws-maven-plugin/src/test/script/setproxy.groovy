/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 Oracle and/or its affiliates. All rights reserved.
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

log.info("Checking proxy...")
def itsettings = new XmlParser().parse(project.build.testResources.directory[0] + "/it-settings.xml")
def itproxy = ""
if (settings?.proxies) {
    Node proxies = new Node(itsettings, "proxies")
    settings?.proxies?.each { proxy ->
        if (proxy.active) {
            if ("http".equals(proxy.protocol)) {
                itproxy +=  " -Dhttp.proxyHost=" + proxy.host
                if (proxy.port) {
                    itproxy += " -Dhttp.proxyPort=" + proxy.port
                }
            } else if ("https".equals(proxy.protocol)) {
                itproxy +=  " -Dhttps.proxyHost=" + proxy.host
                if (proxy.port) {
                    itproxy += " -Dhttps.proxyPort=" + proxy.port
                }
            }
            def p = new Node(proxies, "proxy")
            new Node(p, "protocol", proxy.protocol)
            new Node(p, "port", proxy.port)
            if (proxy.username) {new Node(p, "username", proxy.username)}
            if (proxy.password) {new Node(p, "password", proxy.password)}
            new Node(p, "host", proxy.host)
            new Node(p, "active", proxy.active)
            new Node(p, "nonProxyHosts", proxy.nonProxyHosts)
        }
    }
}

if (itproxy.trim().length() > 0) {
    log.info("Setting: " + itproxy.trim())
} else {
    log.info("No proxy found")
}

def writer = new FileWriter(new File(project.build.directory, "it-settings.xml"))
XmlNodePrinter printer = new XmlNodePrinter(new PrintWriter(writer))
printer.setPreserveWhitespace(true);
printer.print(itsettings)

project.getModel().addProperty("ittest-proxy", itproxy.trim())

