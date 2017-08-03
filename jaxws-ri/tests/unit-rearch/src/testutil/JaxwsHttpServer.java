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

package testutil;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser.AdapterFactory;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.HttpAdapterList;
import com.sun.xml.ws.transport.http.server.EndpointImpl;
import com.sun.xml.ws.transport.local.FileSystemResourceLoader;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.FileSet;

import javax.xml.ws.Endpoint;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class JaxwsHttpServer {
    
    private HttpServer appServer;
    private ExecutorService appExecutorService;
    private HttpServer adminServer;
    private ExecutorService adminExecutorService;

    private String sepChar;
    private File webappsDir;
    private File classesDir;
    private Map<String, WarInfo> deployedWARs;
    private boolean stopped;
    
    public JaxwsHttpServer() throws Exception {
        //String homeDir = System.getProperty ("user.dir");
        String j2seServerDir = System.getProperty ("j2se.server.home");
        System.out.println("Server dir="+j2seServerDir);
        sepChar = System.getProperty ("file.separator");
        webappsDir = new File(j2seServerDir+sepChar+"webapps");
        classesDir = new File(webappsDir, "classes");
        System.out.println("webapps dir="+webappsDir.getAbsolutePath());
        deployedWARs = new HashMap<String, WarInfo>();
        start();
    }
    
    public static void main(String[] args) throws Exception {
        new JaxwsHttpServer();   
    }
    
    public void createDeployThread() {
       new Thread(new DeployWAR()).start();
    }
    
    public class WarInfo {
        
    }
    
    public synchronized boolean isStopped() {
        return stopped;
    }
    
    public synchronized void stopped() {
        stopped = true;
    }
    
    public class DeployWAR implements Runnable {
        public void run() {
            System.out.println("Starting DeployWAR thread");
            while(!isStopped()) {
                try {
                    File[] fileList = webappsDir.listFiles();
					if (fileList != null) {
                        for(File file : fileList) {
                            if (file.isFile()) {
                                deployWAR(file.getName());
                            }
                        }
					}
                    Thread.sleep(10000);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Stopping DeployWAR thread");
        }
    }
    
    public void deployWAR(String warName) throws Exception {
        try {
            if (deployedWARs.get(warName) == null) {
				try {
                    System.out.println("Deploying "+warName);
                    File warDirFile = expandWAR(warName);
                    for(Adapter adapter : parseSunJaxws(warDirFile)) {
                       createEndpoint(adapter, warDirFile);
                    }
				} finally {
					// So that we don't try the same failed endpoint
            	    deployedWARs.put(warName, new WarInfo());
				}
            }
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }
    
    public void deployWAR(HttpExchange msg, String war) throws Exception {
        deployWAR(war);
        writeStatus(msg, 200, "DEPLOY OK "+war);
    }
    
    private File expandWAR(String war) throws IOException {
        String warDirName = war.substring(0, war.length()-4);
        File warDirFile = new File(webappsDir, warDirName);
        if (warDirFile.exists()) {
            System.out.println("Already expanded "+war);
            return warDirFile;
        }
        
        System.out.println("Expanding war "+war);
        File src = new File(webappsDir, war);
        System.out.println("WAR path="+src.getAbsolutePath());
        System.out.println("WAR dest path="+warDirFile.getAbsolutePath());
        Expand e = new Expand();
        e.setProject(new Project());
        e.setDest(warDirFile);
        e.setSrc(src);
        e.setTaskType("unzip");
        e.execute();
        
        System.out.println("Copying classes for "+war);
        FileSet srcSet = new FileSet();
        srcSet.setDir(new File(warDirFile, "WEB-INF/classes"));
        srcSet.setIncludes("**");
        Copy copy = new Copy();
        copy.setProject(new Project());
        copy.setTodir(classesDir);
        copy.addFileset(srcSet);
        copy.setTaskType("copy");
        copy.execute();
        
        return warDirFile;
    }

    static final class Adapter extends HttpAdapter {
        final String urlPattern;

        public Adapter(WSEndpoint endpoint, String urlPattern, AdapterList owner ) {
            super(endpoint,owner);
            this.urlPattern = urlPattern;
        }
    }

    static final class AdapterList extends HttpAdapterList<Adapter> implements AdapterFactory<Adapter> {
        protected Adapter createHttpAdapter(String name, String urlPattern, WSEndpoint<?> endpoint) {
            return new Adapter(endpoint,urlPattern,this);
        }
    }

    private List<Adapter> parseSunJaxws(File warDirFile) throws Exception {
        File ddFile = new File(warDirFile, "WEB-INF"+sepChar+"sun-jaxws.xml");
        System.out.println("dd file="+ddFile.getName());
        /*
        String classesDir = userDir+sepChar+"webapps"+sepChar+warDir+sepChar+
            "WEB-INF"+sepChar+"classes";
        System.out.println("classes dir="+classesDir);
        URL url = new File(classesDir).toURL();
        ClassLoader urlc = new URLClassLoader(new URL[] { url }, 
                    this.getClass().getClassLoader());
         */
        DeploymentDescriptorParser<Adapter> parser = new DeploymentDescriptorParser<Adapter>(
                this.getClass().getClassLoader(), new FileSystemResourceLoader(warDirFile), null,
                new AdapterList()
            );
        return parser.parse(ddFile);
    }
    
    private void createEndpoint(Adapter adapter, File warDirFile)
    throws Exception {
        /*
        String url = "http://localhost:8080/"+warDir+endpointInfo.getUrlPattern();
        EndpointFactory.newInstance ().publish (url, endpointInfo.getImplementor());
         */
        
        String urlPattern = adapter.urlPattern;
        if (urlPattern.endsWith("/*")) {
            urlPattern = urlPattern.substring(0, urlPattern.length() - 2);
        }
        String warDirName = warDirFile.getName();
        String contextRoot = "/"+warDirName+urlPattern;
        System.out.println("Context Root="+contextRoot);
        HttpContext context = appServer.createContext (contextRoot);
        
        // Creating endpoint from backdoor (and this publishes it, too)
        Endpoint endpoint = new EndpointImpl(adapter.getEndpoint(),context);

        //// set MTOM
        //if (binding instanceof SOAPBinding) {
        //    ((SOAPBinding)endpoint.getBinding()).setMTOMEnabled(((SOAPBinding)binding).isMTOMEnabled());
        //    ((SOAPBinding)endpoint.getBinding()).setRoles(((SOAPBinding)binding).getRoles());
        //}
    }
    
    public void collectMetadata(File wsdlDirFile, List<File> metadataFile) {
        File[] files = wsdlDirFile.listFiles();
        if (files == null) {
            return;
        }
        for(File file : files) {
            if (file.isDirectory()) {
                collectMetadata(file, metadataFile);
                continue;
            }
            metadataFile.add(file);
        }
    }
            
    public void undeployWAR(HttpExchange msg, String war) throws IOException {
        System.out.println("Undeploy war="+war);
        writeStatus(msg, 200, "UNDEPLOY OK "+war);
    }
    
    public void stop(HttpExchange msg) throws IOException {
        System.out.println("Stop ");
        stopped();
        appServer.stop(2);
        appExecutorService.shutdown();
        System.out.println("AppServer Stopped ");
        writeStatus(msg, 200, "STOP OK ");
        adminServer.stop(2);
        adminExecutorService.shutdown();
        System.out.println("AdminServer Stopped ");
    }
    
    private void startAppServer() throws Exception {
        appServer = HttpServer.create(new InetSocketAddress(8080), 5);
        appExecutorService = Executors.newFixedThreadPool(5);
        appServer.setExecutor(appExecutorService);
        appServer.start();
        System.out.println("AppServer started");
    }
            
    private void start() throws Exception {
        // Create App Server
        startAppServer();
        // Create Admin Server
        startAdminServer();
        // Create deploy Thread
        createDeployThread();
    }
    
    public void startAdminServer() throws Exception {
        // Create admin server
        InetSocketAddress inetAddress = new InetSocketAddress(9999);
        adminServer = HttpServer.create(inetAddress, 5);
        adminExecutorService = Executors.newFixedThreadPool(5);
        adminServer.setExecutor(adminExecutorService);
        HttpContext context = adminServer.createContext("/admin");
        context.setHandler(new HttpHandler() {
            public void handle(HttpExchange msg) {
                try {
                    System.out.println("Received HTTP request:"+msg.getRequestURI());
                    String method = msg.getRequestMethod();
                    if (method.equals("GET")) {
                        InputStream is = msg.getRequestBody();
                        readFully(is);
                        is.close();
                        writeGetReply(msg);
                    } else {
                        System.out.println("****** METHOD not handled ***** "+method);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                } finally {
                    msg.close();
                }
            }
        });
        adminServer.start();
        System.out.println("AdminServer started");
    }
    
    protected void writeGetReply(HttpExchange msg)
    throws Exception {
     
        String queryString = msg.getRequestURI().getQuery();
        System.out.println("queryString="+queryString);
        if (queryString.startsWith("deploy=")) {
            int index = queryString.indexOf("=");
            String warName = null;
            if (index != -1) {
                warName = queryString.substring(index+1);
                deployWAR(msg, warName);
            } else {
                writeStatus(msg, 200, "Don't know "+queryString);
            }
        } else if (queryString.startsWith("undeploy=")) {
            int index = queryString.indexOf("=");
            String warName = null;
            if (index != -1) {
                warName = queryString.substring(index+1);
                undeployWAR(msg, warName);
            } else {
                writeStatus(msg, 200, "Don't know "+queryString);
            }
        } else if (queryString.startsWith("stop")) {
            stop(msg);
        } else {
            writeStatus(msg, 200, "Don't know "+queryString);
        }
         
    }
    
    /*
     * writes 404 Not found error html page
     */
    private void writeStatus(HttpExchange msg, int code, String message)
    throws IOException {
        msg.getResponseHeaders().add("Content-Type", "text/html");
        msg.sendResponseHeaders(code, 0);
        OutputStream outputStream = msg.getResponseBody();
        PrintWriter out = new PrintWriter(outputStream);
        out.println("<html><head><title>");
        out.println("JaxwsHttpServer Status");
        out.println("</title></head><body>");
        out.println(message);
        out.println("</body></html>");
        out.close();
    }
    
    /*
     * Consumes the entire input stream
     */
    private static void readFully(InputStream is) throws IOException {
        byte[] buf = new byte[1024];
        if (is != null) {
            while (is.read(buf) != -1);
        }
    }
}
