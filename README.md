
# HDFS Shell UI (CLI tool)
HDFS Shell is a HDFS manipulation tool to work with [functions integrated in Hadoop DFS](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/FileSystemShell.html)

![Image of HDFS-Shell](https://github.com/avast/hdfs-shell/blob/master/web/screencast.gif)

[![Build Status - Master](https://travis-ci.org/avast/hdfs-shell.svg?branch=master)](https://travis-ci.org/avast/hdfs-shell) ![Linux](https://img.shields.io/badge/os-linux-green.svg?style=flat) ![Windows](https://img.shields.io/badge/os-windows-green.svg?style=flat) ![Apache 2](https://img.shields.io/badge/license-Apache2-blue.svg?style=flat)


## Purpose
There are 3 possible usecases:

- Running user interactive UI shell, inserting command by user
- Launching Shell with specific HDFS command
- Running in daemon mode - communication using UNIX domain sockets

###  Why such UI shell?

#### Advantages UI against direct calling hdfs dfs function:

- HDFS DFS initiates JVM for each command call, HDFS Shell does it only once - which means great speed enhancement when you need to work with HDFS more often
- Commands can be used in short way - eg. ```hdfs dfs -ls /```, ```ls /``` - both will work
- **HDFS path completion using TAB key**
- you can easily add any other HDFS manipulation function
- there is a command history persisting in history log (~/.hdfs-shell/hdfs-shell.log)
- **support for relative directory + commands ```cd``` and ```pwd```**
- advanced commands like ```su```, ```groups```, ```whoami```
- customizable shell prompt

#### Disadvantages UI against direct calling hdfs dfs function:

- commands cannot be piped, eg: calling ```ls /analytics | less ``` is not possible at this time, you have to use HDFS Shell in Daemon mode

## Using HDFS Shell UI

### Launching HDFS Shell UI
#### Requirements:
- JDK 1.8
- It's working on both Windows/Linux Hadoop 2.6.0

#### Download
- [Download binary](https://github.com/avast/hdfs-shell/releases/download/v1.0.7/hdfs-shell-1.0.7.zip)

#### Configuring launch script(s) for your environment
HDFS-Shell is a standard Java application. For its launch you need to define 2 things on your classpath:

1. All ```./lib/*.jar``` on classpath (the dependencies ```./lib``` are included in the binary bundle or they are located in Gradle build/distributions/*.zip)
2. Path to directory with your Hadoop Cluster config files (hdfs-site.xml, core-site.xml etc.) - without these files the HDFS Shell will work in local filesystem mode
 - on Linux it's usually located in ```/etc/hadoop/conf``` folder
 - on Windows it's usually located in ```%HADOOP_HOME%\etc\hadoop\``` folder

Note that paths inside java -cp switch are separated by ```:``` on Linux and ```;``` on Windows.

Pre-defined launch scripts are located in the zip file. You can modify it locally as needed.

- for CLI UI run ```hdfs-shell.sh``` (without parameters) otherwise:
- HDFS Shell can be launched directly with the command to execute - after completion, hdfs-shell will exit
- launch HDFS with ```hdfs-shell.sh script <file_path>``` to execute commands from file
- launch HDFS with ```hdfs-shell.sh xscript <file_path>``` to execute commands from file but ignore command errors (skip errors)

#### Possible commands inside shell

- type ```help``` to get list of all supported commands
- ```clear``` or ```cls``` to clear screen
- ```exit``` or ```quit``` or just ```q``` to exit the shell
- for calling system command type ```! <command>``` , eg. ```! echo hello``` will call the system command echo
- type (hdfs) command only without any parameters to get its parameter description, eg. ```ls``` only
- ```script <file_path>``` to execute commands from file
- ```xscript <file_path>``` to execute commands from file but ignore command errors (skip errors)

##### Additional commands
For our purposes we also integrated following commands:
- ```set showResultCodeON```  and ```set showResultCodeOFF``` - if it's enabled, it will write command result code after its completion
- ```cd```, ```pwd```
- ```su <username>``` - ***experimental*** -  changes current active user - it won't probably work on secured HDFS (KERBEROS)
- ```whoami``` - prints effective username
- ```groups <username1 <username2,...>>``` - eg.```groups hdfs``` prints groups for given users, same as ```hdfs groups my_user my_user2``` functionality
- ```edit 'my file'``` - see the config below


###### Edit Command
Since the version 1.0.4 the simple command 'edit' is available. The command gets selected file from HDFS to the local temporary directory and launches the editor. Once the editor saves the file (with a result code 0), the file is uploaded back into HDFS (target file is overwritten).
By default the editor path is taken from ```$EDITOR``` environment variable. If ```$EDITOR``` is not set, ```vim``` (Linux, Mac) or ```notepad.exe``` (Windows) is used.

###### How to change command (shell) prompt
HDFS Shell supports customized bash-like prompt setting!
I implemented support for these switches listed in this [table](https://bash.cyberciti.biz/guide/Changing_bash_prompt) (include colors!, exclude ```\!, \#```).
You can also use this [online prompt generator](http://ezprompt.net/) to create prompt value of your wish.
To setup your favorite prompt simply add ```export HDFS_SHELL_PROMPT="value"``` to your .bashrc (or set env variable on Windows) and that's it. Restart HDFS Shell to apply change.
Default value is currently set to ```\e[36m\u@\h \e[0;39m\e[33m\w\e[0;39m\e[36m\\$ \e[37;0;39m```.

### Running Daemon mode
![Image of HDFS-Shell](https://github.com/avast/hdfs-shell/blob/master/web/screenshot2.png)

- run hdfs-shell-daemon.sh
- then communicate with this daemon using UNIX domain sockets - eg. ```echo ls / | nc -U /var/tmp/hdfs-shell.sock```



## Project programming info
The project is using Gradle 3.x to build. By default it's using Hadoop 2.6.0, but it also has been succesfully tested with version 2.7.x.
It's based on [Spring Shell](https://github.com/spring-projects/spring-shell) (includes JLine component). Using Spring Shell mechanism you can easily add your own commands into HDFS Shell.
(see com.avast.server.hdfsshell.commands.ContextCommands or com.avast.server.hdfsshell.commands.HadoopDfsCommands for more details)

**All suggestions and merge requests are welcome.**

#### Other tech info:
For developing, add to JVM args in your IDE launch config dialog: 
``` -Djline.WindowsTerminal.directConsole=false -Djline.terminal=jline.UnsupportedTerminal```


#### Known limitations & problems

- There is a problem with a parsing of commands containing a file or directory including a space - eg. it's not possible to create directory ```My dir``` using command ```mkdir "My dir"``` . This should be probably resolved with an upgrade to Spring Shell 2.
- It's not possible to remove root directory (```rm -R dir```) from root (```/```) directory. You have to use absolut path instead (```rm -R /dir```). It's caused by bug in Hadoop. See [HADOOP-15233](https://issues.apache.org/jira/browse/HADOOP-15233) for more details. Removing directory from another cwd is not affected. 

### Contact
Author&Maintainer: Ladislav Vitasek  - vitasek/@/avast.com
