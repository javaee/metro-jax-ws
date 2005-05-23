/**
 * $Id: TypeMoniker.java,v 1.1 2005-05-23 23:23:51 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.modeler.annotation;

import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.tools.ws.processor.modeler.annotation.*;

/**
 *
 * @author  dkohlert
 */
public interface TypeMoniker {

    public TypeMirror create(AnnotationProcessorEnvironment apEnv);
}
