package com.avast.server.hdfsshell.commands;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.shell.Command;
import org.apache.hadoop.fs.shell.CommandFactory;
import org.apache.hadoop.fs.shell.FsCommand;
import org.mvnsearch.spring.boot.shell.ClientConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("SameParameterValue")
@Component
public class HadoopDfsCommands implements CommandMarker {


    private final List<String> NO_PARAMS_COMMANDS = Arrays.asList("-ls", "-lsr");


    final ContextCommands contextCommands;

    @Autowired
    public HadoopDfsCommands(ContextCommands contextCommands) {
        this.contextCommands = contextCommands;
    }


    @CliAvailabilityIndicator({"appendToFile", "cat", "checksum", "chgrp", "chmod", "chown", "copyFromLocal", "copyToLocal", "count", "cp", "createSnapshot", "deleteSnapshot", "df", "du", "dus", "expunge", "get", "getfacl", "getfattr", "getmerge", "ls", "ll", "lsr", "mkdir", "moveFromLocal", "moveToLocal", "mv", "put", "renameSnapshot", "rm", "rmdir", "rmr", "setfacl", "setfattr", "setrep", "stat", "tail", "test", "text", "touchz"})
    public boolean isSimpleAvailable() {
        //always available
        return true;
    }

    @CliCommand(value = {"appendToFile", "hdfs dfs -appendToFile"}, help = "Appends the contents of all the given local files to the given dst file.")
    public String appendToFile(
            @CliOption(key = {""}, help = "Appends the contents of all the given local files to the given dst file.") String path
    ) {
        return runCommand("appendToFile", path);
    }


    @CliCommand(value = {"cat", "hdfs dfs -cat"}, help = "Fetch all files that match the file pattern <src> and display their content on stdout.")
    public String cat(
            @CliOption(key = {""}, help = "Fetch all files that match the file pattern <src> and display their content on stdout.") String path
    ) {
        return runCommand("cat", path);
    }


    @CliCommand(value = {"checksum", "hdfs dfs -checksum"}, help = "Dump checksum information for files that match the file pattern <src> to stdout.")
    public String checksum(
            @CliOption(key = {""}, help = "Dump checksum information for files that match the file pattern <src> to stdout.") String path
    ) {
        return runCommand("checksum", path);
    }


    @CliCommand(value = {"chgrp", "hdfs dfs -chgrp"}, help = "This is equivalent to -chown .")
    public String chgrp(
            @CliOption(key = {""}, help = "This is equivalent to -chown .") String path
    ) {
        return runCommand("chgrp", path);
    }


    @CliCommand(value = {"chmod", "hdfs dfs -chmod"}, help = "Changes permissions of a file.")
    public String chmod(
            @CliOption(key = {""}, help = "Changes permissions of a file.") String path
    ) {
        return runCommand("chmod", path);
    }


    @CliCommand(value = {"chown", "hdfs dfs -chown"}, help = "Changes owner and group of a file.")
    public String chown(
            @CliOption(key = {""}, help = "Changes owner and group of a file.") String path
    ) {
        return runCommand("chown", path);
    }


    @CliCommand(value = {"copyFromLocal", "hdfs dfs -copyFromLocal"}, help = "Identical to the -put command.")
    public String copyFromLocal(
            @CliOption(key = {""}, help = "Identical to the -put command.") String path
    ) {
        return runCommand("copyFromLocal", path);
    }


    @CliCommand(value = {"copyToLocal", "hdfs dfs -copyToLocal"}, help = "Identical to the -get command.")
    public String copyToLocal(
            @CliOption(key = {""}, help = "Identical to the -get command.") String path
    ) {
        return runCommand("copyToLocal", path);
    }


    @CliCommand(value = {"count", "hdfs dfs -count"}, help = "Count the number of directories, files and bytes under the paths that match the specified file pattern.")
    public String count(
            @CliOption(key = {""}, help = "Count the number of directories, files and bytes under the paths that match the specified file pattern.") String path
    ) {
        return runCommand("count", path);
    }


    @CliCommand(value = {"cp", "hdfs dfs -cp"}, help = "Copy files that match the file pattern <src> to a destination.")
    public String cp(
            @CliOption(key = {""}, help = "Copy files that match the file pattern <src> to a destination.") String path
    ) {
        return runCommand("cp", path);
    }


    @CliCommand(value = {"createSnapshot", "hdfs dfs -createSnapshot"}, help = "Create a snapshot on a directory.")
    public String createSnapshot(
            @CliOption(key = {""}, help = "Create a snapshot on a directory.") String path
    ) {
        return runCommand("createSnapshot", path);
    }


    @CliCommand(value = {"deleteSnapshot", "hdfs dfs -deleteSnapshot"}, help = "Delete a snapshot from a directory.")
    public String deleteSnapshot(
            @CliOption(key = {""}, help = "Delete a snapshot from a directory.") String path
    ) {
        return runCommand("deleteSnapshot", path);
    }


    @CliCommand(value = {"df", "hdfs dfs -df"}, help = "Shows the capacity, free and used space of the filesystem.")
    public String df(
            @CliOption(key = {""}, help = "Shows the capacity, free and used space of the filesystem.") String path
    ) {
        return runCommand("df", path);
    }


    @CliCommand(value = {"du", "hdfs dfs -du"}, help = "Show the amount of space, in bytes, used by the files that match the specified file pattern.")
    public String du(
            @CliOption(key = {""}, help = "Show the amount of space, in bytes, used by the files that match the specified file pattern.") String path
    ) {
        return runCommand("du", path);
    }


    @CliCommand(value = {"dus", "hdfs dfs -dus"}, help = "(DEPRECATED) Same as 'du -s'.")
    public String dus(
            @CliOption(key = {""}, help = "(DEPRECATED) Same as 'du -s'.") String path
    ) {
        return runCommand("dus", path);
    }


    @CliCommand(value = {"expunge", "hdfs dfs -expunge"}, help = "Empty the Trash.")
    public String expunge(
            @CliOption(key = {""}, help = "Empty the Trash.") String path
    ) {
        return runCommand("expunge", path);
    }


    @CliCommand(value = {"get", "hdfs dfs -get"}, help = "Copy files that match the file pattern <src> to the local name.")
    public String get(
            @CliOption(key = {""}, help = "Copy files that match the file pattern <src> to the local name.") String path
    ) {
        return runCommand("get", path);
    }


    @CliCommand(value = {"getfacl", "hdfs dfs -getfacl"}, help = "Displays the Access Control Lists (ACLs) of files and directories.")
    public String getfacl(
            @CliOption(key = {""}, help = "Displays the Access Control Lists (ACLs) of files and directories.") String path
    ) {
        return runCommand("getfacl", path);
    }


    @CliCommand(value = {"getfattr", "hdfs dfs -getfattr"}, help = "Displays the extended attribute names and values (if any) for a file or directory.")
    public String getfattr(
            @CliOption(key = {""}, help = "Displays the extended attribute names and values (if any) for a file or directory.") String path
    ) {
        return runCommand("getfattr", path);
    }


    @CliCommand(value = {"getmerge", "hdfs dfs -getmerge"}, help = "Get all the files in the directories that match the source file pattern and merge and sort them to only one file on local fs.")
    public String getmerge(
            @CliOption(key = {""}, help = "Get all the files in the directories that match the source file pattern and merge and sort them to only one file on local fs.") String path
    ) {
        return runCommand("getmerge", path);
    }


    @CliCommand(value = {"ls", "hdfs dfs -ls"}, help = "List the contents that match the specified file pattern.")
    public String ls(
            @CliOption(key = {""}, help = "List the contents that match the specified file pattern.", specifiedDefaultValue = "", unspecifiedDefaultValue = "") String path
    ) {
        if (StringUtils.isEmpty(path)) {
            path = null;
        }
        return runCommand("ls", path);
    }

    @CliCommand(value = {"ll"}, help = "List the contents that match the specified file pattern.")
    public String ll(
            @CliOption(key = {""}, help = "List the contents that match the specified file pattern.") String path
    ) {
        return ls("");
    }


    @CliCommand(value = {"lsr", "hdfs dfs -lsr"}, help = "(DEPRECATED) Same as 'ls -R'.")
    public String lsr(
            @CliOption(key = {""}, help = "(DEPRECATED) Same as 'ls -R'.") String path
    ) {
        if (StringUtils.isEmpty(path)) {
            path = null;
        }
        return runCommand("lsr", path);
    }


    @CliCommand(value = {"mkdir", "hdfs dfs -mkdir"}, help = "Create a directory in specified location.")
    public String mkdir(
            @CliOption(key = {""}, help = "Create a directory in specified location.") String path
    ) {
        return runCommand("mkdir", path);
    }


    @CliCommand(value = {"moveFromLocal", "hdfs dfs -moveFromLocal"}, help = "Same as -put, except that the source is deleted after it's copied.")
    public String moveFromLocal(
            @CliOption(key = {""}, help = "Same as -put, except that the source is deleted after it's copied.") String path
    ) {
        return runCommand("moveFromLocal", path);
    }


    @CliCommand(value = {"moveToLocal", "hdfs dfs -moveToLocal"}, help = "Not implemented yet.")
    public String moveToLocal(
            @CliOption(key = {""}, help = "Not implemented yet.") String path
    ) {
        return runCommand("moveToLocal", path);
    }


    @CliCommand(value = {"mv", "hdfs dfs -mv"}, help = "Move files that match the specified file pattern <src> to a destination <dst>.")
    public String mv(
            @CliOption(key = {""}, help = "Move files that match the specified file pattern <src> to a destination <dst>.") String path
    ) {
        return runCommand("mv", path);
    }


    @CliCommand(value = {"put", "hdfs dfs -put"}, help = "Copy files from the local file system into fs.")
    public String put(
            @CliOption(key = {""}, help = "Copy files from the local file system into fs.") String path
    ) {
        return runCommand("put", path);
    }


    @CliCommand(value = {"renameSnapshot", "hdfs dfs -renameSnapshot"}, help = "Rename a snapshot from oldName to newName.")
    public String renameSnapshot(
            @CliOption(key = {""}, help = "Rename a snapshot from oldName to newName.") String path
    ) {
        return runCommand("renameSnapshot", path);
    }


    @CliCommand(value = {"rm", "hdfs dfs -rm"}, help = "Delete all files that match the specified file pattern.")
    public String rm(
            @CliOption(key = {""}, help = "Delete all files that match the specified file pattern.") String path
    ) {
        return runCommand("rm", path);
    }


    @CliCommand(value = {"rmdir", "hdfs dfs -rmdir"}, help = "Removes the directory entry specified by each directory argument, provided it is empty.")
    public String rmdir(
            @CliOption(key = {""}, help = "Removes the directory entry specified by each directory argument, provided it is empty.") String path
    ) {
        return runCommand("rmdir", path);
    }


    @CliCommand(value = {"rmr", "hdfs dfs -rmr"}, help = "(DEPRECATED) Same as 'rm -r'.")
    public String rmr(
            @CliOption(key = {""}, help = "(DEPRECATED) Same as 'rm -r'.") String path
    ) {
        return runCommand("rmr", path);
    }


    @CliCommand(value = {"setfacl", "hdfs dfs -setfacl"}, help = "Sets Access Control Lists (ACLs) of files and directories.")
    public String setfacl(
            @CliOption(key = {""}, help = "Sets Access Control Lists (ACLs) of files and directories.") String path
    ) {

        return runSetFaclCommand("setfacl", path);
    }


    @CliCommand(value = {"setfattr", "hdfs dfs -setfattr"}, help = "Sets an extended attribute name and value for a file or directory.")
    public String setfattr(
            @CliOption(key = {""}, help = "Sets an extended attribute name and value for a file or directory.") String path
    ) {
        return runCommand("setfattr", path);
    }


    @CliCommand(value = {"setrep", "hdfs dfs -setrep"}, help = "Set the replication level of a file.")
    public String setrep(
            @CliOption(key = {""}, help = "Set the replication level of a file.") String path
    ) {
        return runCommand("setrep", path);
    }


    @CliCommand(value = {"stat", "hdfs dfs -stat"}, help = "Print statistics about the file/directory at <path> in the specified format.")
    public String stat(
            @CliOption(key = {""}, help = "Print statistics about the file/directory at <path> in the specified format.") String path
    ) {
        return runCommand("stat", path);
    }


    @CliCommand(value = {"tail", "hdfs dfs -tail"}, help = "Show the last 1KB of the file.")
    public String tail(
            @CliOption(key = {""}, help = "Show the last 1KB of the file.") String path
    ) {
        return runCommand("tail", path);
    }


    @CliCommand(value = {"test", "hdfs dfs -test"}, help = "Answer various questions about <path>, with result via exit status.")
    public String test(
            @CliOption(key = {""}, help = "Answer various questions about <path>, with result via exit status.") String path
    ) {
        return runCommand("test", path);
    }


    @CliCommand(value = {"text", "hdfs dfs -text"}, help = "Takes a source file and outputs the file in text format.")
    public String text(
            @CliOption(key = {""}, help = "Takes a source file and outputs the file in text format.") String path
    ) {
        return runCommand("text", path);
    }


    @CliCommand(value = {"touchz", "hdfs dfs -touchz"}, help = "Creates a file of zero length at <path> with current time as the timestamp of that <path>.")
    public String touchz(
            @CliOption(key = {""}, help = "Creates a file of zero length at <path> with current time as the timestamp of that <path>.") String path
    ) {
        return runCommand("touchz", path);
    }

    private String runSetFaclCommand(String command, String pathCommand) {
        if (pathCommand == null) {
            return runCommand(command, pathCommand);
        }

        final String[] arguments = pathCommand.split(" ");
        int foundPath = -1;
        for (int i = 0; i < arguments.length; i++) {
            String argument = arguments[i];
            if (argument.startsWith("/") && argument.contains(",")) {
                foundPath = i;
                break;
            }
        }
        if (foundPath >= 0) {
            final String[] paths = arguments[foundPath].split(",");
            if (paths.length > 1) {
                final int pathIndex = foundPath;
                Arrays.stream(paths).forEach(path -> {
                    arguments[pathIndex] = path;
                    runCommand(command, arguments);
                });
                return "";
            }
        }

        return runCommand(command, arguments);
    }

    private static String[] replaceHdfsPath(String[] pathArguments) {
        for (int i = 0; i < pathArguments.length; i++) {
            String pathArgument = pathArguments[i];
            if (pathArgument.startsWith("/hdfs/")) {
                pathArguments[i] = pathArguments[i].replace("/hdfs", "");
            }
        }
        return pathArguments;
    }

    String runCommand(String cmdName, String path) {
        final String[] args;
        if (path == null) {
            args = new String[0];
        } else {
            args = path.trim().split(" ");
        }
        return runCommand(cmdName, args);
    }


    String runCommand(String cmdName, String[] arguments)  {
        final Configuration conf = contextCommands.getConfiguration();

        cmdName = "-" + cmdName;
        final Command command = getCommandInstance(cmdName, conf);
        if (command == null) {
            return "Unknown command " + cmdName;
        }

        if (arguments.length == 0 && !NO_PARAMS_COMMANDS.contains(cmdName)) {
            return command.getDescription();
        }


        contextCommands.updateCurrentWorkingDirectory();

        arguments = replaceHdfsPath(arguments);
        final PrintStream printStream = ClientConnection.context.get();
        if (printStream != null) {
            command.err = printStream;
            command.out = printStream;
        }
//        final ByteArrayOutputStream out = new ByteArrayOutputStream();
//        final PrintStream printStream = new PrintStream(out);
//        command.err = printStream;
//        command.out = printStream;

        final int result = command.run(arguments);
        if (contextCommands.isShowResultCode()) {
            if (result == 0) {
                command.out.println("Exit code = " + result);
            } else {
                command.err.println("Exit code = " + result);
            }
        }

        //printStream.close();
        if (result != 0 && contextCommands.isFailOnError()) {
            throw new RuntimeException("HDFS Command finished with result code " + result);
        }
        return "";
    }

    private static Command getCommandInstance(String cmdName, Configuration conf) {
        final CommandFactory commandFactory = new CommandFactory(conf);
        FsCommand.registerCommands(commandFactory);
        return commandFactory.getInstance(cmdName, conf);
    }


}
