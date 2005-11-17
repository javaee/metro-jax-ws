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

