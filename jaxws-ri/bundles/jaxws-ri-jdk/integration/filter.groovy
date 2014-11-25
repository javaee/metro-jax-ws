#!/usr/local/bin/groovy
/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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

import groovy.transform.Field

import java.util.regex.Pattern

File dir = new File(".")
//File dir = new File("/Users/.../jdk9u-cpu")
File file = new File(dir.getAbsolutePath() + "/" + args[0]);
File revertScript = new File(dir.getAbsolutePath() + "/" + args[0] + "-revert.sh");
File changesLst = new File(dir.getAbsolutePath() + "/" + args[0] + "-changes.lst");

backup(revertScript)
backup(changesLst)

def backup(File f) {
    if (f.exists()) {
        f.renameTo(f.getAbsolutePath()+".bak")
    }
}

String NEW_FILE = "diff "
String OLD_CNTNT = "-"
String NEW_CNTNT = "+"
String [] IGNORE_CNTNT = ["+++ ", "--- "]
String DELIM_CNTNT = "@@ "

println("Result file: " + revertScript.getAbsolutePath())
println("Changes file: " + changesLst.getAbsolutePath())
println("")

revertScript.createNewFile();
changesLst.createNewFile();

revertScript.append("#!/bin/bash -ex\n")

def diff = null
def changes = []
Change change = null

def modified = 0
def reverted = 0

file.eachLine { line ->

    boolean skip = false;
    for (String prefix : IGNORE_CNTNT) {
        if (line.startsWith(prefix)) skip = true;
    }

    if (skip) {
        // nothing to do ...
    } else if (line.startsWith(NEW_FILE)) {

        if (diff != null) {
            processChange(change, changes)
            if (processFile(diff, changes, revertScript, changesLst)) {
                modified++
            } else {
                reverted++
            }
        }

        change = new Change(line)
        changes = []
        diff = line
        //print(getFileName(diff)+": ")

    } else if (line.startsWith(OLD_CNTNT)) {
        if (change == null) println("ERROR - unknown file yet: " + line);

        change.addOldValue(line.substring(1))
    } else if (line.startsWith(DELIM_CNTNT)) {
        if (change != null) {  // first change
            processChange(change, changes);
        }
        change = new Change(diff)

    } else if (line.startsWith(NEW_CNTNT)) { // new value expected
        if (change == null) println("ERROR - unknown file yet: " + line);

        change.addNewValue(line.substring(1))
    }
}


processChange(change, changes);
if (processFile(diff, changes, revertScript, changesLst)) {
    modified++
} else {
    reverted++
}

println()
println("modified: " + modified)
println("revreted: " + reverted)

changesLst.append("\n\nmodified: " + modified)
changesLst.append("\nrevreted: " + reverted)


//println(revertScript.getText())

void processChange( Change change, def changes ) {
    if (!change.canBeSkipped()) {
        if (debug) print "."
        changes.add(change)
    } else {
        if (debug) print "@"
    }
}
boolean processFile( String diff, def changes, def revertScript, def changesLst) {
    if (changes.size() > 0) {
        println " =========================================================================================================== "
        println " changes:" + changes.size() + ", " + getFileName(diff)
        println " =========================================================================================================== "
        for(Change ch:changes) {
            println("CHANGE: " + ch.pos + " ------")
            println(""+ ch)
        }
        changesLst.append(" changes:" + changes.size() + ", " + getFileName(diff) + "\n")
        return true
    } else {
        if (debug) println " no changes, revreting " + getFileName(diff)

        revertScript.append("hg revert --no-backup " + getFileName(diff) + ";\n")
        return false
    }
}

class Change {

    String pos
    String oldValue
    String newValue
    Change(pos) {
        this.pos = pos
        oldValue = ""
        newValue = ""
    }

    void addOldValue(String val) {
        oldValue += separator(oldValue)
        oldValue += val
    }

    void addNewValue(String val) {
        newValue += separator(newValue)
        newValue += val
    }

    String separator(String val) {
        return val.size() > 0? "\n" : "";
    }

    /**
     * Code here all the conditions for reverting changes - copyrights, whitespace differences etc.
     * @return
     */
    boolean canBeSkipped() {
       return (oldValue != null && oldValue.matches("(.*)Copyright (.*) Oracle and/or its affiliates. All rights reserved.\$") &&
                (newValue != null && newValue.matches("(.*)Copyright (.*) Oracle and/or its affiliates. All rights reserved.\$"))) ||
        oldValue.trim().equals(newValue.trim()) ||
        oldValue.equals("/**\n * Copyright (c) 2001, Thai Open Source Software Center Ltd") ||
        oldValue.contains("@since") ||
        newValue.contains("@since");
    }

    public String toString() {
        return "- old ------\n" + oldValue +
            "\n- new ------\n" + newValue +
                "\n------------";
    }

}

String getFileName(String value) {
    int idx = value.lastIndexOf(" ")

    return idx > -1 ?  value.substring(idx) : value;
}

@Field
boolean debug = true;