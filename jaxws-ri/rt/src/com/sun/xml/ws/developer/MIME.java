package com.sun.xml.ws.developer;

import javax.xml.ws.spi.WebServiceFeatureAnnotation;
import java.lang.annotation.*;

/**
 * This feature represents the use of MIME attachments with a
 * web service.
 *
 * <pre>
 * for e.g.: To keep all MIME attachments in memory
 *
 * <p>
 * @WebService
 * @MIME(allMemory=true)
 * public class HelloService {
 * }
 * </pre>
 *
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
     * MIME message is parsed eagerly
     */
    boolean parseEagerly() default false;

    /**
     * All the attachments are kept in memory
     */
    boolean allMemory() default false;

    /**
     * After this threshold(no of bytes), large
     * attachments are written to file system
     */
    int memoryThresold() default 1048576;
}
