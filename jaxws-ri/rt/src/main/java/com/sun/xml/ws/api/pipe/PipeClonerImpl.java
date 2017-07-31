/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;

/**
 * Clones the whole pipeline.
 *
 * <p>
 * Since {@link Pipe}s may form an arbitrary directed graph, someone needs
 * to keep track of isomorphism for a clone to happen correctly. This class
 * serves that role.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("deprecation")
public class PipeClonerImpl extends PipeCloner {

    private static final Logger LOGGER = Logger.getLogger(PipeClonerImpl.class.getName());

    // no need to be constructed publicly. always use the static clone method.
    /*package*/ public PipeClonerImpl() {
    	super(new HashMap<Object,Object>());
    }
    
    protected PipeClonerImpl(Map<Object,Object> master2copy) {
    	super(master2copy);
    }

    /**
     * {@link Pipe} version of {@link #copy(Tube)}
     */
    @SuppressWarnings("unchecked")
	public <T extends Pipe> T copy(T p) {
        Pipe r = (Pipe)master2copy.get(p);
        if(r==null) {
            r = p.copy(this);
            // the pipe must puts its copy to the map by itself
            assert master2copy.get(p)==r : "the pipe must call the add(...) method to register itself before start copying other pipes, but "+p+" hasn't done so";
        }
        return (T)r;
    }


    /**
     * The {@link Pipe} version of {@link #add(Tube, Tube)}.
     */
    public void add(Pipe original, Pipe copy) {
        assert !master2copy.containsKey(original);
        assert original!=null && copy!=null;
        master2copy.put(original,copy);
    }

    /**
     * Disambiguation version.
     */
    public void add(AbstractTubeImpl original, AbstractTubeImpl copy) {
        add((Tube)original,copy);
    }

	@Override
	public void add(Tube original, Tube copy) {
        assert !master2copy.containsKey(original);
        assert original!=null && copy!=null;
        master2copy.put(original,copy);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Tube> T copy(T t) {
        Tube r = (Tube)master2copy.get(t);
        if(r==null) {
            if (t != null) {
              r = t.copy(this);
            } else {
              if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.fine("WARNING, tube passed to 'copy' in " + this + " was null, so no copy was made");
              }
            }
        }
        return (T)r;
	}
}
