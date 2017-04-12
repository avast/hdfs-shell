#!/bin/bash
CWD=$(cd $(dirname $0); pwd)  

java -Ddaemon=true -Xms200m -Xmx400m -cp ${CWD}/lib/*:/etc/hadoop/conf com.avast.server.hdfsshell.MainApp "$@"
