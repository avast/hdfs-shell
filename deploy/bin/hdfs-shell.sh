#!/bin/bash
java -Xms200m -Xmx400m -cp ./lib/*:/etc/hadoop/conf com.avast.server.hdfsshell.MainApp "$@"