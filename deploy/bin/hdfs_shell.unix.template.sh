#!/usr/bin/env bash
SCRIPT_FULL_PATH="\$(readlink -f "\${BASH_SOURCE[0]}")"
BIN_DIR="\$(dirname "\${SCRIPT_FULL_PATH}")"
LIB_DIR="\$(readlink -f "\${BIN_DIR}/../lib")"
HADOOP_DIR="\${HADOOP_CONF_DIR:-/etc/hadoop/conf}"
HADOOP_CLASSPATH="\${HADOOP_CLASSPATH:-\$(hadoop classpath)}"
command_args=(java -Xms200m -Xmx400m -cp "\${LIB_DIR}/*:\${HADOOP_DIR}:\${HADOOP_CLASSPATH}")
echo "Launching HDFS Shell with HADOOP_CONF_DIR: \${HADOOP_DIR}"

while (( \$# > 0 )); do
  case "\$1" in
    -d|--daemon) command_args+=('-Ddaemon=true'); shift;;
  esac
  shift
done
command_args+=( ${mainClassName} "\$@")
command "\${command_args[@]}"