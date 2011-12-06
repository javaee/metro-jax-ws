/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.api.pipe;

import java.util.Map;

/**
 * Clones the whole pipeline.
 *
 * <p>
 * Since {@link Tube}s may form an arbitrary directed graph, someone needs
 * to keep track of isomorphism for a clone to happen correctly. This class
 * serves that role.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class TubeCloner {
    // Pipe to pipe, or tube to tube
    public final Map<Object,Object> master2copy;

    /**
     * Invoked by a client of a tube to clone the whole pipeline.
     *
     * <p>
     * {@link Tube}s implementing the {@link Tube#copy(TubeClonerImpl)} method
     * shall use {@link #copy(Tube)} method.
     *
     * @param p
     *      The entry point of a pipeline to be copied. must not be null.
     * @return
     *      The cloned pipeline. Always non-null.
     */
    @SuppressWarnings("deprecation")
	public static Tube clone(Tube p) {
        // we often want to downcast TubeCloner to PipeCloner,
        // so let's create PipeCloner to make that possible
        return new PipeClonerImpl().copy(p);
    }

    // no need to be constructed publicly. always use the static clone method.
    /*package*/ TubeCloner(Map<Object,Object> master2copy) {
    	this.master2copy = master2copy;
    }

    /**
     * Invoked by a {@link Tube#copy(TubeClonerImpl)} implementation
     * to copy a reference to another pipe.
     *
     * <p>
     * This method is for {@link Tube} implementations, not for users.
     *
     * <p>
     * If the given tube is already copied for this cloning episode,
     * this method simply returns that reference. Otherwise it copies
     * a tube, make a note, and returns a copied tube. This additional
     * step ensures that a graph is cloned isomorphically correctly.
     *
     * <p>
     * (Think about what happens when a graph is A->B, A->C, B->D, and C->D
     * if you don't have this step.)
     *
     * @param t
     *      The tube to be copied.
     * @return
     *      The cloned tube. Always non-null.
     */
	public abstract <T extends Tube> T copy(T t);

    /**
     * This method must be called from within the copy constructor
     * to notify that the copy was created.
     *
     * <p>
     * When your pipe has references to other pipes,
     * it's particularly important to call this method
     * before you start copying the pipes you refer to,
     * or else there's a chance of inifinite loop.
     */
    public abstract void add(Tube original, Tube copy);
}
