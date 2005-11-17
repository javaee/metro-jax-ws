/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.tools.ws.processor.modeler.wsdl;

import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import org.xml.sax.SAXParseException;

import java.util.ResourceBundle;
import java.text.MessageFormat;

public class ConsoleErrorReporter implements ErrorListener{

    private LocalizableMessageFactory messageFactory;
    private ProcessorEnvironment env;
    private boolean printStackTrace;

    public ConsoleErrorReporter(ProcessorEnvironment env, boolean printStackTrace) {
        this.env = env;
        this.printStackTrace = printStackTrace;
        messageFactory =
            new LocalizableMessageFactory("com.sun.tools.ws.resources.model");
    }

    public void error(SAXParseException e) {
        if(printStackTrace)
            e.printStackTrace();
        env.error(messageFactory.getMessage("model.saxparser.exception",
                new Object[]{e.getMessage(), getLocationString(e)}));
    }

    public void fatalError(SAXParseException e) {
        if(printStackTrace)
            e.printStackTrace();

        env.error(messageFactory.getMessage("model.saxparser.exception",
                new Object[]{e.getMessage(), getLocationString(e)}));        
    }

    public void warning(SAXParseException e) {
        env.warn(messageFactory.getMessage("model.saxparser.exception",
                new Object[]{e.getMessage(), getLocationString(e)}));
    }

    /**
     * Used to report possibly verbose information that
     * can be safely ignored.
     */
    public void info(SAXParseException e) {
        env.info(messageFactory.getMessage("model.saxparser.exception",
                new Object[]{e.getMessage(), getLocationString(e)}));
    }

     /**
    * Returns the human readable string representation of the
    * {@link org.xml.sax.Locator} part of the specified
    * {@link SAXParseException}.
    *
    * @return  non-null valid object.
    */
    protected final String getLocationString( SAXParseException e ) {
      if(e.getLineNumber()!=-1 || e.getSystemId()!=null) {
          int line = e.getLineNumber();
          return format("ConsoleErrorReporter.LineXOfY", line==-1?"?":Integer.toString( line ),
              getShortName( e.getSystemId() ) );
      } else {
          return format("ConsoleErrorReporter.UnknownLocation");
      }
    }

    /** Computes a short name of a given URL for display. */
    private String getShortName( String url ) {
        if(url==null)
            return format("ConsoleErrorReporter.UnknownLocation");
        return url;
    }

    private String format( String property, Object... args ) {
        String text = ResourceBundle.getBundle("com.sun.tools.ws.resources.model").getString(property);
        return MessageFormat.format(text,args);
    }

}
