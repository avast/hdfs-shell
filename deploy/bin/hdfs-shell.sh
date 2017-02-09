#!/bin/bash
java -Xms200m -Xmx400m -cp /var/hdfs-shell/lib/*:/etc/hadoop/conf com.avast.server.hdfsshell.MainApp "$@"