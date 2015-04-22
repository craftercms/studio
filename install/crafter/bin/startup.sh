#!/bin/sh
export JAVA_OPTS="-XX:MaxPermSize=512m -Xms128m -Xmx1024m"
cd ..
rm -rf ./apache-tomcat/logs
mkdir ./apache-tomcat/logs
touch ./apache-tomcat/logs/catalina.out
sh ./apache-tomcat/bin/startup.sh run &
tail -f ./apache-tomcat/logs/catalina.out &
cd ./bin