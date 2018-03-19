/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jvnet.jax_ws_commons.jaxws;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Lukas Jungmann
 */
public class RepositoryITCase {

    /**
     * only two 'releases' of JAX-WS RI should be referenced/downloaded to local
     * (test) repo: 2.1.7 and the latest integrated one
     */
    @Test
    public void riVersions() {
        File projects = new File(System.getProperty("it.projects.dir"));
        File wsDir = new File(new File(projects.getParentFile(), "it-repo"), "com/sun/xml/ws");
        FilenameFilter ff = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        };

        File versions = new File(wsDir, "jaxws-rt");
        List<String> list = Arrays.asList(versions.list(ff));
        Assert.assertEquals(list.size(), 1);
        Assert.assertTrue(list.contains(System.getProperty("jaxws-ri.version")));

        versions = new File(wsDir, "jaxws-tools");
        list = Arrays.asList(versions.list(ff));
        Assert.assertEquals(list.size(), 1);
        Assert.assertTrue(list.contains(System.getProperty("jaxws-ri.version")));
    }
}
