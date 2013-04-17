#!/bin/bash -x
binding=ri

rm -f $JAXWS_HOME/lib/*eclipselink*.jar;
rm -f $JAXWS_HOME/lib/*sdo*.jar;

# set up catalina base and start server instance
export CATALINA_BASE=$PWD/tomcat-instance
if [ -d "$CATALINA_BASE" ]
then
chmod -R u+w $CATALINA_BASE
fi
rm -rf $CATALINA_BASE
mkdir $CATALINA_BASE
mkdir $CATALINA_BASE/shared
mkdir $CATALINA_BASE/shared/lib
mkdir $CATALINA_BASE/logs
mkdir $CATALINA_BASE/work

chmod -R u+w $CATALINA_HOME/common
#mkdir $CATALINA_HOME/common/endorsed

mkdir $CATALINA_BASE/temp
mkdir $CATALINA_BASE/webapps
cp -r $CATALINA_HOME/conf $CATALINA_BASE
chmod -R u+w $CATALINA_BASE/conf
sed -e s:8080:$port:g -e s:8005:$shutdownport:g $CATALINA_BASE/conf/server-minimal.xml > $CATALINA_BASE/conf/server.xml
cp $JAXWS_HOME/lib/*jar $CATALINA_BASE/shared/lib

export JAVA_ENDORSED_DIRS=$CATALINA_HOME/common/endorsed
cp $JAXWS_HOME/lib/jaxb-api.jar $JAVA_ENDORSED_DIRS
cp $JAXWS_HOME/lib/jaxws-api.jar $JAVA_ENDORSED_DIRS

export TOMCAT_HOME=$CATALINA_BASE
export ANT_OPTS="-Djava.endorsed.dirs=$JAVA_ENDORSED_DIRS"

# run all the unit tests

# log file name
httpLogFile=$PWD/httpTest.log

OS=`uname`
case $OS in
   	Linux)
		AWK="awk";
        DEPLOY_TMP="/tmp"
		;;
	SunOS)
		AWK="nawk";
        DEPLOY_TMP="/var/tmp"
		;;
	*)
		AWK="awk";
        DEPLOY_TMP="/tmp"
		;;
esac

XMLS=`find src -name build.properties -print | grep config | sed -e s:config/build.properties:build.xml:g`
echo ""
PARAMS="-Dxmlformatter=true"

#Stop server instance if its still running from previous execution
echo "Stopping Tomcat instance on port:$port"
$CATALINA_HOME/bin/shutdown.sh

echo "Starting TOMCAT"
echo "Starting Tomcat instance on port:$port"
$CATALINA_HOME/bin/startup.sh
#echo "WAITING 3 mins for TOMCAT to come up"
#sleep 150

echo ""
echo "--------- Running http transport tests --------------"
>$httpLogFile

    callant.sh clean compile_test_util

for file in $XMLS
do
    echo "Deploying server [$file] (http transport)"
    callant.sh -f $file clean build deploy $PARAMS 2>&1 | tee -a $httpLogFile | egrep "(^Tests run:|ERROR|FAILED)"
done
sleep 50
for file in $XMLS
do
    callant.sh -f $file clean build runclient $PARAMS 2>&1 | tee -a $httpLogFile | egrep "(^Tests run:|ERROR|FAILED)"
done
echo "-----------------------------------------------------"

echo "Stopping TOMCAT"
$CATALINA_HOME/bin/shutdown.sh
