/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * $Id: DirectoryUtil.java,v 1.3 2005-08-08 21:42:18 kohlert Exp $
 */

package com.sun.tools.ws.processor.util;

import java.io.File;

import com.sun.tools.ws.processor.generator.GeneratorException;
import com.sun.tools.ws.util.ClassNameInfo;

/**
 * Util provides static utility methods used by other wscompile classes.
 *
 * @author WS Development Team
 */
public class DirectoryUtil  {

    public static File getOutputDirectoryFor(String theClass,
        File rootDir, ProcessorEnvironment env) throws GeneratorException {

        File outputDir = null;
        String qualifiedClassName = theClass;
        String packagePath = null;
        String packageName = ClassNameInfo.getQualifier(qualifiedClassName);
        if (packageName != null && packageName.length() > 0) {
            packagePath = packageName.replace('.', File.separatorChar);
        }

        // Do we have a root directory?
        if (rootDir != null) {

            // Yes, do we have a package name?
            if (packagePath != null) {

                // Yes, so use it as the root. Open the directory...
                outputDir = new File(rootDir, packagePath);

                // Make sure the directory exists...
                ensureDirectory(outputDir,env);
            } else {

                // Default package, so use root as output dir...
                outputDir = rootDir;
            }
        } else {

            // No root directory. Get the current working directory...
            String workingDirPath = System.getProperty("user.dir");
            File workingDir = new File(workingDirPath);

            // Do we have a package name?
            if (packagePath == null) {

                // No, so use working directory...
                outputDir = workingDir;
            } else {

                // Yes, so use working directory as the root...
                outputDir = new File(workingDir, packagePath);

                // Make sure the directory exists...
                ensureDirectory(outputDir,env);
            }
        }

        // Finally, return the directory...
        return outputDir;
    }

    private static void ensureDirectory(File dir, ProcessorEnvironment env)
        throws GeneratorException {

        if (!dir.exists()) {
            dir.mkdirs();
            if (!dir.exists()) {
                throw new GeneratorException("generator.cannot.create.dir",
                    dir.getAbsolutePath());
            }
        }
    }
}

