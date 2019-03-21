#!/bin/bash
SCRIPT_DIR="$( cd "$( dirname "$(readlink -f "${BASH_SOURCE[0]}" )" )" && pwd )"
HADOOP_DIR=${HADOOP_CONF_DIR:-/etc/hadoop/conf}

echo "Launching HDFS Shell with HADOOP_CONF_DIR: ${HADOOP_DIR}"

java -Xms200m -Xmx400m -cp ${SCRIPT_DIR}/../lib/*:${HADOOP_DIR} com.avast.server.hdfsshell.MainApp "$@"
