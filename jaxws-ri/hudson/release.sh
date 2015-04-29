#!/bin/sh
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
# or packager/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at packager/legal/LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

while getopts :r:l:t:s:b:j:m:u:p:w:dh arg
do
    case "$arg" in
        r)  RELEASE_VERSION="${OPTARG:?}" ;;
        b)  RELEASE_BRANCH="${OPTARG:?}" ;;
        j)  JAXB_VERSION="${OPTARG:?}" ;;
        t)  GIT_VERSION="${OPTARG:?}" ;;
        l)  MAVEN_USER_HOME="${OPTARG:?}" ;;
        s)  MAVEN_SETTINGS="${OPTARG:?}" ;;
        m)  SOURCES_VERSION="${OPTARG:?}" ;;
        w)  WORKROOT="${OPTARG:?}" ;;
        u)  WWW_SVN_USER="${OPTARG:?}" ;;
        p)  WWW_SVN_PASSWORD="${OPTARG:?}" ;;
        d)  debug=true ;;
        h)
            echo "Usage: release.sh [-r RELEASE_VERSION] --mandatory, the release version string, for example 2.2.11"
            echo "                   [-t GIT_VERSION] --mandatory, git tag or sha to checkout for the release"
            echo "                   [-b RELEASE_BRANCH] --optional, the branch for the release"
            echo "                   [-j JAXB_VERSION] --optional, the branch for the release"
            echo "                   [-l MVEN_USER_HOME] -- optional, alternative maven local repository location"
            echo "                   [-w WORKROOT] -- optional, default is current dir (`pwd`)"
            echo "                   [-m SOURCES_VERSION] -- optional, version in pom.xml need to be repaced with \$RELEASE_VERSION, default is \${RELEASE_VERSION}-SNAPSHOT"
            echo "                   [-s MAVEN_SETTINGS] --optional, alternative maven settings.xml"
            echo "                   [-u WWW_SVN_USER] --optional, the svn scm username for commit the www docs, if not specified it uses cached credential"
            echo "                   [-p WWW_SVN_PASSWORD] --optional the svn scm password for commit the www docs"
            echo "                   [-d] -- debug mode"
            exit ;;
        "?")
            echo "ERROR: unknown option \"$OPTARG\"" 1>&2
            echo "" 1>&2 ;;
    esac
done

if [ "$M2_HOME" = "" -o ! -d $M2_HOME ]; then
    echo "ERROR: Check your M2_HOME: $M2_HOME"
    exit 1
fi

if [ "$JAVA_HOME" = "" -o ! -d $JAVA_HOME ]; then
    echo "ERROR: Check your JAVA_HOME: $JAVA_HOME"
    exit 1
fi
export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH
export TMPDIR=${TMPDIR:-/tmp}

PROXYURL=www-proxy.us.oracle.com
PROXYPORT=80

export http_proxy=$PROXYURL:$PROXYPORT
export https_proxy=$http_proxy


export MAVEN_OPTS="-Xms256m -Xmx768m -XX:PermSize=256m -XX:MaxPermSize=512m -Dhttp.proxyHost=$PROXYURL -Dhttp.proxyPort=$PROXYPORT -Dhttps.proxyHost=$PROXYURL -Dhttps.proxyPort=$PROXYPORT"

if [ "$MAVEN_USER_HOME" = "" ]; then
     user=${LOGNAME:-${USER-"`whoami`"}}
     MAVEN_USER_HOME="/scratch/$user/.m2/repository"
fi

if [ -n "$MAVEN_SETTINGS" ]; then
    MAVEN_SETTINGS="-s $MAVEN_SETTINGS"
fi

if [ "$WORKROOT" = "" ]; then
    WORKROOT=`pwd`
fi

if [ "$MAVEN_USER_HOME" = "" ]; then
    MAVEN_LOCAL_REPO="-Dmaven.repo.local=${WORKROOT}/.m2/repository"
else
    MAVEN_LOCAL_REPO="-Dmaven.repo.local=${MAVEN_USER_HOME}"
fi

if [ "$RELEASE_VERSION" = "" ]; then
    echo "ERROR: you need to give the -r with the release revision"
    exit 1
fi

echo "Release on git version: $GIT_VERSION"
if [ "$GIT_VERSION" = "" ]; then
   exit 1;
fi

cd $WORKROOT || {
    echo "ERROR: fail to cd to working dir $WORKROOT"
    exit 1
}

if [ -e jaxws-ri ] ; then
   echo "INFO: Removing old JAXWS workspace"
   rm -rf jaxws-ri
fi

echo "INFO: Cloning jaxws-ri git repository"
git clone git@orahub.oraclecorp.com:fmw-infra-metro/jaxws-ri.git || {
    echo "fail to clone the git repository"
    exit 1
}

# create release branch from the given git version to be released
cd jaxws-ri
RELEASE_BRANCH=${RELEASE_BRANCH:-`echo jaxws${RELEASE_VERSION} |sed 's/\.//g'`}
echo "INFO: git checkout -b $RELEASE_BRANCH $GIT_VERSION"
git checkout -b $RELEASE_BRANCH $GIT_VERSION || {
    echo "ERROR: fail to checkout $GIT_VRSION to branch $RELEASE_BRANCH"
    exit 1
}
GIT_SHA=`git rev-parse --short HEAD`
echo "INFO: release $RELEASE_VERSION base on the git sha $GIT_SHA"
SOURCES_VERSION=${SOURCES_VERSION:-"${RELEASE_VERSION}-SNAPSHOT"}
echo "INFO: Replacing project version $SOURCES_VERSION in sources with new release version $RELEASE_VERSION"
find ./ -name "pom.xml" | while read file; do
    return_status=0
    echo "INFO: Editing $file ..."
    perl -i -pe "s|${SOURCES_VERSION}|${RELEASE_VERSION}|g" $file || {
        echo "ERROR: Fail replace version $RELEASE_VERSION on $file"
        return_status=1
    }
    grep -q "<version>$RELEASE_VERSION<\/version>" $file
    if [ $? -ne 0 ]; then
        echo "ERROR: release version $RELEASE_VERSION not found in $file"
        return_status=1
    fi
    if [ $return_status -eq 1 ]; then
        exit 1
    fi
done
if [ -n "$JAXB_VERSION" ]; then
    perl -i -pe "s|<jaxb.version>.*</jaxb.version>|<jaxb.version>$JAXB_VERSION</jaxb.version>|g" jaxws-ri/boms/bom/pom.xml
fi

if [ $? -ne 0 ]; then
    echo "ERROR: fail to replace release version $RELEASE_VERSION to pom.xml files"
    exit 1 
fi

git diff --quiet 
if [ $? -eq 0 ]; then
    echo "WARNING: no change found on pom.xml for the release"
else
    git commit -a -m "release version $RELEASE_VERSION" || {
        echo "ERROR: fail to commit the modified pom.xml"
        exit 1
    }
fi   
  
if [ "$debug" = "true" ]; then
    echo "DEBUG: build while no deploy"
    echo "INFO:  mvn $MAVEN_SETTINGS -B -C -f jaxws-ri/pom.xml $MAVEN_LOCAL_REPO -DskipTests=true -Prelease-profile,release-sign-artifacts -Dlicense.url=http://hudson-sca.us.oracle.com/job/tlda-license/lastSuccessfulBuild/artifact/ clean install"
    mvn $MAVEN_SETTINGS -B -C -f jaxws-ri/pom.xml $MAVEN_LOCAL_REPO -DskipTests=true -Prelease-profile,release-sign-artifacts -Dlicense.url=http://hudson-sca.us.oracle.com/job/tlda-license/lastSuccessfulBuild/artifact/ clean install
else
    echo "INFO: Build and Deploy ..."
    echo "INFO:  mvn $MAVEN_SETTINGS -B -C -f jaxws-ri/pom.xml $MAVEN_LOCAL_REPO -DskipTests=true -Prelease-profile,release-sign-artifacts -Dlicense.url=http://hudson-sca.us.oracle.com/job/tlda-license/lastSuccessfulBuild/artifact/ clean install deploy"
    mvn $MAVEN_SETTINGS -B -C -f jaxws-ri/pom.xml $MAVEN_LOCAL_REPO -DskipTests=true -Prelease-profile,release-sign-artifacts -Dlicense.url=http://hudson-sca.us.oracle.com/job/tlda-license/lastSuccessfulBuild/artifact/ clean install deploy
fi
if [ $? -ne 0 ]; then
      exit 1
fi
echo "INFO: Tagging release $RELEASE_VERSION"
RELEASE_TAG=`echo "JAXWS_$RELEASE_VERSION" |sed 's/\./_/g'`
echo "INFO: git tag -m \"Tag release $RELEASE_VERSION\" $RELEASE_TAG"
git tag -m "Tag release $RELEASE_VERSION" $RELEASE_TAG

if [ "$debug" = "true" ]; then
    echo "DEBUG: debug only, no push ..."
else
    git push origin $RELEASE_BRANCH
    git push origin $RELEASE_VERSION
fi

cd $WORKROOT
echo "INFO: Updating www docs ..."
if [ -d "www" ]; then
    rm -rf www
fi
echo "INFO: svn checkout --non-interactive --depth=empty https://svn.java.net/svn/jax-ws~wcr/trunk/www"
svn checkout --non-interactive --depth=empty https://svn.java.net/svn/jax-ws~wcr/trunk/www || {
    echo "ERROR: checkout www failed"
    exit 1
}
# create www release folder and copy the built out docs
cd www || {
    echo "ERROR: fail chdir to www"
    exit 1
}
mkdir -p $RELEASE_VERSION/docs $RELEASE_VERSION/javadocs/rt $RELEASE_VERSION/javadocs/tools
echo "INFO: cp $WORKROOT/jaxws-ri/jaxws-ri/target/jaxws-ri-$RELEASE_VERSION-src-licensee.zip $RELEASE_VERSION/"
cp $WORKROOT/jaxws-ri/jaxws-ri/target/jaxws-ri-$RELEASE_VERSION-src-licensee.zip $RELEASE_VERSION/ || {
    echo "ERROR: fail copy jaxws-ri-$RELEASE_VERSION-src-licensee.zip"
    exit 1
}
echo "INFO: cp $WORKROOT/jaxws-ri/jaxws-ri/docs/www/target/www-stage/index.html $RELEASE_VERSION/"
cp $WORKROOT/jaxws-ri/jaxws-ri/docs/www/target/www-stage/index.html $RELEASE_VERSION/ || {
    echo "ERROR: fail copy index.html"
    exit 1
}
echo "INFO: cp -r $WORKROOT/jaxws-ri/jaxws-ri/docs/release-documentation/target/docbook/* $RELEASE_VERSION/docs/"
cp -r $WORKROOT/jaxws-ri/jaxws-ri/docs/release-documentation/target/docbook/* $RELEASE_VERSION/docs/ || {
    echo "ERROR: fail copy docbook"
    exit 1
}
find $RELEASE_VERSION/docs -type f -name "*.fo" |xargs rm -f
(
    cd $WORKROOT/www/$RELEASE_VERSION/javadocs/rt
    jar -xf $WORKROOT/jaxws-ri/jaxws-ri/bundles/jaxws-rt/target/jaxws-rt-$RELEASE_VERSION-javadoc.jar
) || {
    echo "ERROR: fail extract jaxws-rt-$RELEASE_VERSION-javadoc.jar"
    exit 1
}
(
    cd $WORKROOT/www/$RELEASE_VERSION/javadocs/tools
    jar -xf $WORKROOT/jaxws-ri/jaxws-ri/bundles/jaxws-tools/target/jaxws-tools-$RELEASE_VERSION-javadoc.jar
) || {
    echo "ERROR: fail extract jaxws-tools-$RELEASE_VERSION-javadoc.jar"
    exit 1
}
cd $WORKROOT/www
svn add --non-interactive $RELEASE_VERSION

# link the latest relase to current release
echo "INFO: Update latest download page link to $RELEASE_VERSION"
svn --non-interactive update -q latest
sed -i "s#URL=https://jax-ws.java.net/.*/#URL=https://jax-ws.java.net/$RELEASE_VERSION/#" latest/download.html
sed -i "s#URL=https://jax-ws.java.net/.*/docs/#URL=https://jax-ws.java.net/$RELEASE_VERSION/docs/#" latest/docs.html

# modify www/downloads/ri/index.html
svn --non-interactive update -q downloads
RELEASE_VERSION_MAIN=${RELEASE_VERSION%.*}
set +e
grep -q "<h2>Latest RI for JAX-WS $RELEASE_VERSION_MAIN<\/h2>" downloads/ri/index.html
if [ $? -eq 0 ]; then
    echo "INFO: found $RELEASE_VERSION_MAIN snippet, updating downloads/ri/index.html to current release..."
    sed -i -e "s#RI Version: $RELEASE_VERSION_MAIN.*#RI Version: $RELEASE_VERSION#" -e "s#<a href=\"http://repo.maven.apache.org/maven2/com/sun/xml/ws/jaxws-ri/$RELEASE_VERSION_MAIN.*/jaxws-ri-$RELEASE_VERSION_MAIN.*.zip\">Binary Distribution</a><br>#<a href=\"http://repo.maven.apache.org/maven2/com/sun/xml/ws/jaxws-ri/$RELEASE_VERSION/jaxws-ri-$RELEASE_VERSION.zip\">Binary Distribution</a><br>#" -e "s#<a href=\"http://repo.maven.apache.org/maven2/com/sun/xml/ws/jaxws-ri-src/$RELEASE_VERSION_MAIN.*/jaxws-ri-src-$RELEASE_VERSION_MAIN.*-sources.zip\">Source Distribution</a><br>#<a href=i\"http://repo.maven.apache.org/maven2/com/sun/xml/ws/jaxws-ri-src/$RELEASE_VERSION/jaxws-ri-src-$RELEASE_VERSION-sources.zip\">Source Distribution</a><br>#" -e "s#<a href=\"/$RELEASE_VERSION_MAIN.*\">More about RI $RELEASE_VERSION_MAIN.* release</a><br>#<a href=\"/$RELEASE_VERSION\">More about RI $RELEASE_VERSION release</a><br>#" downloads/ri/index.html
else
    echo "INFO: adding new $RELEASE_VERSION_MAIN snippet into downloads/ri/index.html"
    tmpfile=$TMPDIR/release_jaxws_newentry_$$
    rm -f $tmpfile
    cat > $tmpfile <<EOF

<h2>Latest RI for JAX-WS $RELEASE_VERSIN_MAIN</h2>
RI Version: $RELEASE_VERION<br>
<a href="http://repo.maven.apache.org/maven2/com/sun/xml/ws/jaxws-ri/$RELEASE_VERSION/jaxws-ri-$RELEASE_VERSION.zip">Binary Distribution</a><br>
<a href="http://repo.maven.apache.org/maven2/com/sun/xml/ws/jaxws-ri-src/$RELEASE_VERSION/jaxws-ri-src-$RELEASE_VERSION-sources.zip">Source Distribution</a><br>
<a href="/$RELEASE_VERSION">More about RI $RELEASE_VERSION release</a><br>

EOF
    # I maybe figure out a better way of not hardcode the line number here later
    sed  -i "5 r $tmpfile" downloads/ri/index.html
    rm -f $tmpfile
fi

# modify www/__modules/left_sidebar.htmlx
echo "INFO: add $RELEASE_VERSION to the left side bar"
svn --non-interactive update -q __modules
line=`sed -n '/ *<li><a href=\"#\">Download RI<\/a>/=' __modules/left_sidebar.htmlx`
line=`expr $line + 1`
appendLine="\ \ \ \ \ \ \ <li><a href=\"http://jax-ws.java.net/$RELEASE_VERSION\">$RELEASE_VERSION</a>"
sed -i "$line a\
$appendLine" __modules/left_sidebar.htmlx
sed -i -e "s#<li><a href=\"http://jax-ws.java.net/nonav/.*/docs/index.html\">Release documentation</a>#<li><a href=\"http://jax-ws.java.net/nonav/$RELEASE_VERSION/docs/index.html\">Release documentation</a>#" -e "s#<li><a href=\"http://jax-ws.java.net/nonav/jaxws-api/.*/index.html\">API Javadoc</a>#<li><a href=\"http://jax-ws.java.net/nonav/jaxws-api/$RELEASE_VERSION_MAIN/index.html\">API Javadoc</a>#" __modules/left_sidebar.htmlx

if [ -n "$WWW_SVN_USER"  -a -n "$WWW_SVN_PASSWORD" ]; then
    AUTH="--username $WWW_SVN_USER --password $WWW_SVN_PASSWORD --no-auth-cache"
else
    AUTH=""
fi
if [ "$debug" = "true" ]; then
    echo "DEBUG: debug only, not commit the docs."
    echo "DEBUG: svn $AUTH --non-interactive commit -m \"JAXWS release $RELEASE_VERSION\""
else
    echo "INFO: commit the updated docs"
    svn $AUTH --non-interactive commit -m "JAXWS $RELEASE_VERSION" .
    if [ $? -ne 0 ]; then
        echo "ERROR: fail to commit the www docs!"
        exit 1
    fi
fi
