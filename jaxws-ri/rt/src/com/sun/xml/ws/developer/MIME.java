package com.sun.xml.ws.developer;

import javax.xml.ws.spi.WebServiceFeatureAnnotation;
import java.lang.annotation.*;

/**
 * @author Jitendra Kotamraju
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@WebServiceFeatureAnnotation(id = MIMEFeature.ID, bean = MIMEFeature.class)
public @interface MIME {
    /**
     * Directory in which large attachments are stored
     */
    String dir();

    /**
     * The MIME message is parsed eagerly
     */
    boolean parseEagerly();

    /**
     * All the attachments are kept in memory
     */
    boolean allMemory();

    /**
     * After this threshold(no of bytes), large attachments are wriiten to file system
     */
    int memoryThresold();
}
