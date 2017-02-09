package com.avast.server.hdfsshell.commands;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@SuppressWarnings("SameParameterValue")
@Component
public class ContextCommands implements CommandMarker {

    private volatile String currentDir;
    private volatile Configuration configuration;
    private volatile String homeDir;

    private boolean showResultCode = false;
    private boolean failOnError;


    @CliAvailabilityIndicator({"pwd", "cd"})
    public boolean isSimpleAvailable() {
        //always available
        return true;
    }

    @CliCommand(value = "set", help = "Set switch value")
    public String set(@CliOption(key = {""}, help = "showResultCodeON/showResultCodeOFF") String commandSwitch) {
        if (commandSwitch == null) {
            return "possible parameters .... showResultCodeON/showResultCodeOFF";
        }
        if (commandSwitch.startsWith("showResultCode")) {
            showResultCode = "showResultCodeON".equalsIgnoreCase(commandSwitch);
            return commandSwitch + " has been set";
        }
        return "Unknown switch " + commandSwitch;
    }


    @CliCommand(value = "pwd", help = "Shows current dir")
    public String pwd() {
        return getCurrentDir();
    }

    @CliCommand(value = "cd", help = "Changes current dir")
    public String cd(@CliOption(key = {""}, help = "cd [<path>]") String newDir) {
        if (StringUtils.isEmpty(newDir)) {
            newDir = getHomeDir();
        }

        final Path path = (newDir.startsWith("/")) ? new Path(newDir) : new Path(getCurrentDir(), newDir);
        try {
            final FileSystem fs = getFileSystem();
            if (fs.exists(path) && fs.isDirectory(path)) {
                currentDir = path.toUri().getPath();
            } else {
                return "-shell: cd: " + newDir + " No such file or directory";
            }
        } catch (Exception e) {
            return "Change directory failed! " + e.getMessage();
        }
        return "";
    }

    public synchronized String getCurrentDir() {
        if (currentDir == null) {
            try {
                final Path path = new Path(Path.CUR_DIR);
                final FileSystem fs = getFileSystem();
                final FileStatus[] fileStatuses = fs.globStatus(path);
                if (fileStatuses == null || fileStatuses.length == 0) {
                    return "";
                }
                homeDir = currentDir = fileStatuses[0].getPath().toUri().getPath();
            } catch (Exception e) {
                return "";
            }
        }
        return currentDir;
    }

    public synchronized String getHomeDir() {
        if (homeDir == null) {
            getCurrentDir();
        }
        if (homeDir == null) {//in case of failure
            return ".";
        }
        return homeDir;
    }

    public synchronized Configuration getConfiguration() {
        if (configuration == null) {
            configuration = new Configuration();
        }
        return configuration;
    }


    public String convertToDir(String path) {
        final String currentDir = getCurrentDir();
        if (StringUtils.isEmpty(currentDir)) {
            return path;
        }
        if (!path.startsWith("/")) {
            return new Path(currentDir, path).toUri().getPath();
        }
        return path;
    }


    private FileSystem getFileSystem() throws IOException {
        final Configuration conf = getConfiguration();
        return FileSystem.get(conf);
    }

    public boolean isShowResultCode() {
        return showResultCode;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean isFailOnError() {
        return failOnError;
    }


    public void updateCurrentWorkingDirectory() {
        final String currentDir = this.getCurrentDir();
        if (StringUtils.isEmpty(currentDir)) {
            return;
        }
        try {
            FileSystem.get(new Configuration()).setWorkingDirectory(new Path(currentDir));
        } catch (Exception e) {
            //ignore
            // e.printStackTrace();
        }
    }

}
