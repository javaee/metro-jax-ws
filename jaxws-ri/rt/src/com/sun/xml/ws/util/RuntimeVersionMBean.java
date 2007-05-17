

package com.sun.xml.ws.util;

import javax.management.*;
import com.sun.xml.ws.util.RuntimeVersion;

/**
 * @author Jitendra Kotamraju
 */
public interface RuntimeVersionMBean {

    /**
     * Get JAX-WS runtime version
     */
    public String getVersion();

}


