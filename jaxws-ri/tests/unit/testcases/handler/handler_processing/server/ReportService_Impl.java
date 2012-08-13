/**
 * $Id: ReportService_Impl.java,v 1.1 2007-09-22 00:39:25 ramapulavarthi Exp $
 *
 * Copyright 2005 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package handler.handler_processing.server;

import java.util.List;

import handler.handler_processing.common.HandlerTracker;
import handler.handler_processing.common.TestConstants;

/**
 * @author Rama Pulavarthi
 */
@javax.jws.WebService(serviceName="TestService", portName="ReportServicePort", targetNamespace="urn:test", endpointInterface="handler.handler_processing.server.ReportService")
public class ReportService_Impl implements ReportService, TestConstants {

    public void setInstruction(String name, int x) {
        if (HandlerTracker.VERBOSE_HANDLERS) {
            System.out.println("report service setting action " + x +
                    " on handler " + name);
        }
        if (x < 200) {
            HandlerTracker.getServerInstance().setHandlerAction(name, x);
        } else {
            HandlerTracker.getServerInstance().setHandleFaultAction(name, x);
        }
    }
    
    public List<String> getReport(String reportName) {
        if (reportName.equals(REPORT_CALLED_HANDLERS)) {
            return HandlerTracker.getServerInstance().getCalledHandlers();
        }
        if (reportName.equals(REPORT_CLOSED_HANDLERS)) {
            return HandlerTracker.getServerInstance().getClosedHandlers();
        }
        if (reportName.equals(REPORT_DESTROYED_HANDLERS)) {
            return HandlerTracker.getServerInstance().getDestroyedHandlers();
        }
        System.err.println("ERROR: server didn't understand report type: " +
                reportName);
        throw new RuntimeException("invalid understand report type: " +
                reportName);
    }
    
    public void clearHandlerTracker() {
        if (HandlerTracker.VERBOSE_HANDLERS) {
            System.out.println("report service clearing tracker");
        }
        HandlerTracker.getServerInstance().clearAll();
    }
    
    public void clearCalledHandlers() {
        if (HandlerTracker.VERBOSE_HANDLERS) {
            System.out.println("report service clearing called handlers");
        }
        HandlerTracker.getServerInstance().clearCalledHandlers();
    }
    
    // util
    
    // todo: does List<String>.toArray make String[] or Object[]?
    private String [] toStringArray(List<String> list) {
        String [] s = new String[list.size()];
        for (int i=0; i< s.length; i++) {
            s[i] = list.get(i);
        }
        return s;
    }
    
}
