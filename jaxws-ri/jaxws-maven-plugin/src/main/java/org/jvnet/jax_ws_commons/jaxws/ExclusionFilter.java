/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2014 Oracle and/or its affiliates. All rights reserved.
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

import java.util.List;
import org.apache.maven.model.Exclusion;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;

/**
 *
 * @author lukas
 */
final class ExclusionFilter implements DependencyFilter {

    private final List<Exclusion> toExclude;

    public ExclusionFilter(List<Exclusion> toExclude) {
        assert toExclude != null : "Null is not allowed";
        this.toExclude = toExclude;
    }

    @Override
    public boolean accept(DependencyNode node, List<DependencyNode> parents) {
        Artifact a = node.getDependency().getArtifact();
        for (Exclusion e : toExclude) {
            if (e.getGroupId().equals(a.getGroupId())
                    && e.getArtifactId().equals(a.getArtifactId())) {
                return false;
            }
        }
        return true;
    }
}
