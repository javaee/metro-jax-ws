/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.tools.ws.wscompile;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.ws.processor.model.Model;
import java.io.IOException;
import org.xml.sax.SAXException;

/**
 * Add-on that works on the generated source code.
 *
 * <p> This add-on will be called after the default generation has finished.
 *
 * @author Lukas Jungmann
 * @since 2.2.6
 */
public abstract class Plugin {

    /**
     * Gets the option name to turn on this add-on.
     *
     * <p> For example, if "abc" is returned, "-abc" will turn on this plugin. A
     * plugin needs to be turned on explicitly, or else no other methods of {@link Plugin}
     * will be invoked.
     *
     * <p> When an option matches the name returned from this method, WsImport
     * will then invoke {@link #parseArgument(Options, String[], int)}, allowing
     * plugins to handle arguments to this option.
     */
    public abstract String getOptionName();

    /**
     * Gets the description of this add-on. Used to generate a usage screen.
     *
     * @return localized description message. should be terminated by \n.
     */
    public abstract String getUsage();

    /**
     * Parses an option <code>args[i]</code> and augment the <code>opt</code> object
     * appropriately, then return the number of tokens consumed.
     *
     * <p> The callee doesn't need to recognize the option that the
     * getOptionName method returns.
     *
     * <p> Once a plugin is activated, this method is called for options that
     * WsImport didn't recognize. This allows a plugin to define additional
     * options to customize its behavior.
     *
     * <p> Since options can appear in no particular order, WsImport allows
     * sub-options of a plugin to show up before the option that activates a
     * plugin (one that's returned by {@link #getOptionName().)
     *
     * But nevertheless a {@link Plugin} needs to be activated to participate in
     * further processing.
     *
     * @return 0 if the argument is not understood. Otherwise return the number
     * of tokens that are consumed, including the option itself. (so if you have
     * an option like "-foo 3", return 2.)
     * @exception BadCommandLineException If the option was recognized but
     * there's an error. This halts the argument parsing process and causes
     * WsImport to abort, reporting an error.
     */
    public int parseArgument(Options opt, String[] args, int i) throws BadCommandLineException, IOException {
        return 0;
    }

    /**
     * Notifies a plugin that it's activated.
     *
     * <p> This method is called when a plugin is activated through the command
     * line option (as specified by {@link #getOptionName()}.
     *
     * <p> Noop by default.
     *
     */
    public void onActivated(Options opts) throws BadCommandLineException {
        // noop
    }

    /**
     * Run the add-on.
     *
     * <p> This method is invoked after WsImport has internally finished the
     * code generation. Plugins can tweak some of the generated code (or add
     * more code) by altering {@link JCodeModel} obtained from {@link WsimportOptions#getCodeModel()
     * } according to the current
     * {@link Model WSDL model} and {@link WsimportOptions}.
     *
     * <p> Note that this method is invoked only when a {@link Plugin} is
     * activated.
     *
     * @param wsdlModel This object allows access to the WSDL model used for
     * code generation.
     *
     * @param options This object allows access to various options used for code
     * generation as well as access to the generated code.
     *
     * @param errorHandler Errors should be reported to this handler.
     *
     * @return If the add-on executes successfully, return true. If it detects
     * some errors but those are reported and recovered gracefully, return
     * false.
     *
     * @throws SAXException After an error is reported to {@link ErrorHandler},
     * the same exception can be thrown to indicate a fatal irrecoverable error. {@link ErrorHandler}
     * itself may throw it, if it chooses not to recover from the error.
     */
    public abstract boolean run(
            Model wsdlModel, WsimportOptions options, ErrorReceiver errorReceiver) throws SAXException;
}
