/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.avast.server.hdfsshell.ui;

import com.avast.server.hdfsshell.commands.ContextCommands;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.core.AbstractShell;
import org.springframework.shell.plugin.support.DefaultPromptProvider;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Vitasek L.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ShellPromptProvider extends DefaultPromptProvider {

    private static final Logger logger = LoggerFactory.getLogger(ShellPromptProvider.class);

    private static final String DEFAULT_PROMPT = "\033[36m\\u@\\h \033[0;39m\033[33m\\w\033[0;39m\033[36m\\$ \033[37;0;39m";
    final ContextCommands contextCommands;

    private SimpleBashPromptInterpreter simpleBashPromptInterpreter;

    @Autowired
    public ShellPromptProvider(ContextCommands contextCommands) {
        this.contextCommands = contextCommands;
    }

    @PostConstruct
    public void init() {
        setPs1(System.getenv().getOrDefault("HDFS_SHELL_PROMPT", DEFAULT_PROMPT));
    }

    public void setPs1(String ps1) {
        simpleBashPromptInterpreter = new SimpleBashPromptInterpreter.Builder(ps1).
                setAppName(() -> "hdfs-shell").setAppVersion(ShellBannerProvider::versionInfo).
                setLocale(Locale.ENGLISH).
                setUsername(this::getWhoami).
                setIsRoot(this::isRootPrompt).
                setCwdAbsolut(contextCommands::getCurrentDir).
                setCwdShort(this::getShortCwd).
                build();
    }

    private boolean isRootPrompt() {
        final String whoami = this.getWhoami();
        final String[] groupsForUser = contextCommands.getGroupsForUser(whoami);
        if (groupsForUser.length == 0) { //make guess
            return "root".equals(whoami) || "hdfs".equals(whoami);
        }
        final String[] groups = contextCommands.getConfiguration().get("dfs.permissions.superusergroup", "supergroup").split(",");
        final Set<String> adminGroups = Arrays.stream(groups).map(String::trim).collect(Collectors.toSet());
        adminGroups.add("Administrators");//for Windows
        adminGroups.add("hdfs");//special cases
        adminGroups.add("root");
        return Arrays.stream(groupsForUser).anyMatch(adminGroups::contains);
    }

    @Override
    public String getPrompt() {
        return AbstractShell.shellPrompt = simpleBashPromptInterpreter.interpret();
    }

    private String getWhoami() {
        try {
            return contextCommands.whoami();
        } catch (IOException e) {
            logger.error("Failed to get active user", e);
            return "hdfs-shell";
        }
    }

//    private String defaultPrompt() {
//        return AnsiOutput.toString(AnsiColor.CYAN, "hdfs-shell ") +
//                AnsiOutput.toString(AnsiColor.YELLOW, contextCommands.getCurrentDir()) +
//                AnsiOutput.toString(AnsiColor.CYAN, " >", AnsiColor.WHITE);
//    }


    @Override
    public String getProviderName() {
        return "Hdfs-shell prompt";
    }

    private String getShortCwd() {
        String currentDir = contextCommands.getCurrentDir();
        if (currentDir.startsWith("/user/")) {
            final String userHome = "/user/" + this.getWhoami();//call getWhoami later
            if (currentDir.startsWith(userHome)) {
                currentDir = StringUtils.replaceOnce(currentDir, userHome, "~");
            }
        }
        return currentDir;
    }
}
