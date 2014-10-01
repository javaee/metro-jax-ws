#!/usr/local/bin/groovy
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

        revertScript.append("hg revert -C " + getFileName(diff) + ";\n")
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
        oldValue.contains(" @since") ||
        newValue.contains(" @since");
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