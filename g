#!/bin/bash

export GRAILS_OPTS="-server -Xmx2G -Xms128M -XX:PermSize=128m -XX:MaxPermSize=1024m -XX:+UseG1GC"
nohup grails --stacktrace --verbose -Dserver.port=8181 run-app 2>&1 &
