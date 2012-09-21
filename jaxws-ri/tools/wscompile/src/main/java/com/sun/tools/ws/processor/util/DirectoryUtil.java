/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.tools.ws.processor.util;

import com.sun.tools.ws.processor.generator.GeneratorException;
import com.sun.tools.ws.util.ClassNameInfo;

import java.io.File;
import java.io.IOException;

/**
 * Util provides static utility methods used by other wscompile classes.
 *
 * @author WS Development Team
 */
public class DirectoryUtil  {

    public static File getOutputDirectoryFor(String theClass, File rootDir) throws GeneratorException {

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
                ensureDirectory(outputDir);
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
                ensureDirectory(outputDir);
            }
        }

        // Finally, return the directory...
        return outputDir;
    }

    public static String getRelativePathfromCommonBase(File file, File base) throws IOException {
        String basePath = base.getCanonicalPath();
        String filePath = file.getCanonicalPath();
        return filePath.substring(basePath.length());       

    }

    private static void ensureDirectory(File dir) throws GeneratorException {
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created || !dir.exists()) {
                throw new GeneratorException("generator.cannot.create.dir",
                    dir.getAbsolutePath());
            }
        }
    }
}

