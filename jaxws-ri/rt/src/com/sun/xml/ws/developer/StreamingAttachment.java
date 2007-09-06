package com.sun.xml.ws.developer;

import javax.xml.ws.spi.WebServiceFeatureAnnotation;
import java.lang.annotation.*;

/**
 * This feature represents the use of StreamingAttachment attachments with a
 * web service.
 *
 * <pre>
 * for e.g.: To keep all StreamingAttachment attachments in memory
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
@WebServiceFeatureAnnotation(id = StreamingAttachmentFeature.ID, bean = StreamingAttachmentFeature.class)
public @interface StreamingAttachment {
    /**
     * Directory in which large attachments are stored
     */
    String dir();

    /**
     * StreamingAttachment message is parsed eagerly
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
