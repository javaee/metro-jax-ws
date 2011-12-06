/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.api.addressing;

import java.net.URL;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.FeatureConstructor;
import com.sun.xml.ws.api.message.HeaderList;

import javax.xml.ws.WebServiceFeature;

import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;

/**
 * Unsupported RI extension to work around an issue in WSIT.
 *
 * <p>
 * <b>This feature is not meant to be used by a common Web service developer</b> as there
 * is no need to send the above mentioned header for a one-way operation. But these
 * properties may need to be sent in certain middleware Web services.
 *
 * <p>
 * This feature allows ReplyTo, From and RelatesTo Message Addressing Properties
 * to be added for all messages that are sent from the port configured with
 * this annotation. All operations are assumed to be one-way, and
 * this feature should be used for one-way
 * operations only.
 * 
 * If a non-null ReplyTo is specified, then MessageID property is also added.
 *
 * @author Arun Gupta
 */
@ManagedData
public class OneWayFeature extends WebServiceFeature {
    /**
     * Constant value identifying the {@link OneWayFeature}
     */
    public static final String ID = "http://java.sun.com/xml/ns/jaxws/addressing/oneway";

    private WSEndpointReference replyTo;
    private WSEndpointReference sslReplyTo;
    private WSEndpointReference from;
    private WSEndpointReference faultTo;
    private WSEndpointReference sslFaultTo;
    private String relatesToID;
    private boolean useAsyncWithSyncInvoke = false;

    /**
     * Create an {@link OneWayFeature}. The instance created will be enabled.
     */
    public OneWayFeature() {
        this.enabled = true;
    }

    /**
     * Create an {@link OneWayFeature}
     *
     * @param enabled specifies whether this feature should
     *                be enabled or not.
     */
    public OneWayFeature(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Create an {@link OneWayFeature}
     *
     * @param enabled specifies whether this feature should be enabled or not.
     * @param replyTo specifies the {@link WSEndpointReference} of wsa:ReplyTo header.
     */
    public OneWayFeature(boolean enabled, WSEndpointReference replyTo) {
        this.enabled = enabled;
        this.replyTo = replyTo;
    }

    /**
     * Create an {@link OneWayFeature}
     *
     * @param enabled specifies whether this feature should be enabled or not.
     * @param replyTo specifies the {@link WSEndpointReference} of wsa:ReplyTo header.
     * @param from specifies the {@link WSEndpointReference} of wsa:From header.
     * @param relatesTo specifies the MessageID to be used for wsa:RelatesTo header.
     */
    @FeatureConstructor({"enabled","replyTo","from","relatesTo"})
    public OneWayFeature(boolean enabled, WSEndpointReference replyTo, WSEndpointReference from, String relatesTo) {
        this.enabled = enabled;
        this.replyTo = replyTo;
        this.from = from;
        this.relatesToID = relatesTo;
    }

    /**
     * {@inheritDoc}
     */
    @ManagedAttribute
    public String getID() {
        return ID;
    }

    public boolean
    hasSslEprs() {
      return sslReplyTo != null || sslFaultTo != null;
    }

    /**
     * Getter for wsa:ReplyTo header {@link WSEndpointReference} .
     *
     * @return address of the wsa:ReplyTo header
     */
    @ManagedAttribute
    public WSEndpointReference getReplyTo() {
        return replyTo;
    }

    public WSEndpointReference getReplyTo(boolean ssl) {
        return (ssl && sslReplyTo != null) ? sslReplyTo : replyTo;
    }

    /**
     * Setter for wsa:ReplyTo header {@link WSEndpointReference}.
     *
     * @param address
     */
    public void setReplyTo(WSEndpointReference address) {
        this.replyTo = address;
    }

    public WSEndpointReference getSslReplyTo() {
      return sslReplyTo;
    }

    public void setSslReplyTo(WSEndpointReference sslReplyTo) {
      this.sslReplyTo = sslReplyTo;
    }

  /**
     * Getter for wsa:From header {@link WSEndpointReference}.
     *
     * @return address of the wsa:From header
     */
    @ManagedAttribute
    public WSEndpointReference getFrom() {
        return from;
    }

    /**
     * Setter for wsa:From header {@link WSEndpointReference}.
     *
     * @param address of the wsa:From header
     */
    public void setFrom(WSEndpointReference address) {
        this.from = address;
    }

    /**
     * Getter for MessageID for wsa:RelatesTo header.
     *
     * @return address of the wsa:FaultTo header
     */
    @ManagedAttribute
    public String getRelatesToID() {
        return relatesToID;
    }

    /**
     * Setter for MessageID for wsa:RelatesTo header.
     *
     * @param id
     */
    public void setRelatesToID(String id) {
        this.relatesToID = id;
    }

  /**
     * Getter for wsa:FaultTo header {@link WSEndpointReference}.
     *
     * @return address of the wsa:FaultTo header
     */
    public WSEndpointReference getFaultTo() {
        return faultTo;
    }

    public WSEndpointReference getFaultTo(boolean ssl) {
        return (ssl && sslFaultTo != null) ? sslFaultTo : faultTo;
    }

    /**
     * Setter for wsa:FaultTo header {@link WSEndpointReference}.
     *
     * @param address of the wsa:FaultTo header
     */
    public void setFaultTo(WSEndpointReference address) {
        this.faultTo = address;
    }

    public WSEndpointReference getSslFaultTo() {
      return sslFaultTo;
    }

    public void setSslFaultTo(WSEndpointReference sslFaultTo) {
      this.sslFaultTo = sslFaultTo;
    }

    /**
     * Getter for whether async is to be used with sync invoke
     *
     * @return whether async is to be used with sync invoke
     */
    public boolean isUseAsyncWithSyncInvoke() {
    	return useAsyncWithSyncInvoke;
    }

    /**
     *  Setter for whether async is to be used with sync invoke
     *
     * @param useAsyncWithSyncInvoke whether async is to be used with sync invoke
     */
    public void setUseAsyncWithSyncInvoke(boolean useAsyncWithSyncInvoke) {
    	this.useAsyncWithSyncInvoke = useAsyncWithSyncInvoke;
    }

    /**
     * Calculate a new EPR using an existing one and substituting SSL specific
     * host and port values.
     * @param epr Existing EPR that will be the starting point for the SSL
     *        version
     * @param sslHost New SSL host or null if the existing host should be used
     * @param sslPort New SSL port or -1 if the existing port should be used
     * @return
     */
    public static WSEndpointReference
    enableSslForEpr(@NotNull WSEndpointReference epr,
                    @Nullable String sslHost,
                    int sslPort) {
        if (!epr.isAnonymous()) {
            String address = epr.getAddress();
            URL url;
            try {
              url = new URL(address);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
            String protocol = url.getProtocol();
            if (!protocol.equalsIgnoreCase("https")) {
              protocol = "https";
              String host = url.getHost();
              if (sslHost != null) {
                host = sslHost;
              }
              int port = url.getPort();
              if (sslPort > 0) {
                port = sslPort;
              }
              try {
                url = new URL(protocol, host, port, url.getFile());
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
              address = url.toExternalForm();
              return
                new WSEndpointReference(address, epr.getVersion());
            }
        }

        return epr;
    }

}
