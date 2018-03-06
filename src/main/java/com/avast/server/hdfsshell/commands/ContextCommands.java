package com.avast.server.hdfsshell.commands;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.hdfs.NameNodeProxies;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.tools.GetUserMappingsProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@SuppressWarnings("SameParameterValue")
@Component
public class ContextCommands implements CommandMarker {
    private static final Logger logger = LoggerFactory.getLogger(ContextCommands.class);

    private String currentDir;
    private Configuration configuration;
    private String homeDir;

    private boolean showResultCode = false;
    private boolean failOnError;

    private GetUserMappingsProtocol userMappingsProtocol;

    @PostConstruct
    public void init() {
        try {
            final HdfsConfiguration conf = new HdfsConfiguration();
            userMappingsProtocol = NameNodeProxies.createProxy(conf, FileSystem.getDefaultUri(conf),
                    GetUserMappingsProtocol.class).getProxy();
        } catch (Exception e) {
            logger.error("Failed to create proxy to get user groups", e);
        }
    }

    public String[] getGroupsForUser(String username) {
        if (userMappingsProtocol != null) {
            try {
                return userMappingsProtocol.getGroupsForUser(username);
            } catch (IOException e) {
                return new String[0];
            }
        }
        return new String[0];
    }

    @CliAvailabilityIndicator({"pwd", "cd", "groups"})
    public boolean isSimpleAvailable() {
        //always available
        return true;
    }

    @CliCommand(value = "groups", help = "Get groups for user")
    public String groups(@CliOption(key = {""}) String username) throws IOException {
        if (StringUtils.isEmpty(username)) {
            username = whoami();
        }
        final StringBuilder result = new StringBuilder();
        Arrays.stream(username.split("\\W+")).forEach((user) -> {
                    if (!user.trim().isEmpty()) {
                        user = user.trim();
                        if (result.length() > 0) {
                            result.append(System.lineSeparator());
                        }
                        result.append(user).append(" : ").append(Arrays.stream(this.getGroupsForUser(user)).collect(Collectors.joining(" ")));
                    }
                }
        );
        return result.toString();
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

    @CliCommand(value = "su", help = "Changes current active user [*experimental*]")
    public synchronized String su(@CliOption(key = {""}, help = "su [<username>]") String newUser) throws IOException {
        if (StringUtils.isEmpty(newUser)) {
            return "No username is defined! ";
        }
//        else {
//            newUser = BashUtils.parseArguments(newUser)[0];
//        }
        final FileSystem fs = getFileSystem();
        final Path usersDir = new Path("/user");
        if (fs.exists(usersDir)) {
            final String finalNewUser = newUser;
            final boolean foundUser = Arrays.stream(fs.listStatus(usersDir)).
                    filter(FileStatus::isDirectory).
                    anyMatch(fileStatus -> fileStatus.getPath().getName().equals(finalNewUser));
            if (!foundUser) {
                return "User " + newUser + " does not exist!";
            }
        }
        System.setProperty("HADOOP_USER_NAME", newUser);
        UserGroupInformation.loginUserFromSubject(null);
        currentDir = null;
        return "";
    }

    @CliCommand(value = "whoami", help = "Print effective username")
    public synchronized String whoami() throws IOException {
        return UserGroupInformation.getCurrentUser().getUserName();
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

    FileSystem getFileSystem() throws IOException {
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
