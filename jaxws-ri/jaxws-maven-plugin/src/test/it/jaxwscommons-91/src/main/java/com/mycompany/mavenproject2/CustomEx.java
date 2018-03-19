/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package com.mycompany.mavenproject2;

import javax.xml.ws.WebFault;

/**
 *
 * @author lukas
 */
@WebFault(name = "SpecificFault")
public class CustomEx extends Exception {

    /**
     * Creates a new instance of
     * <code>CustomEx</code> without detail message.
     */
    public CustomEx() {
    }

    /**
     * Constructs an instance of
     * <code>CustomEx</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public CustomEx(String msg) {
        super(msg);
    }
}
