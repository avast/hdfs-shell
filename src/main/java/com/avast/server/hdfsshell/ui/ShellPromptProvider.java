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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.core.AbstractShell;
import org.springframework.shell.plugin.support.DefaultPromptProvider;
import org.springframework.stereotype.Component;

/**
 * @author Jarred Li
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ShellPromptProvider extends DefaultPromptProvider {

    final ContextCommands contextCommands;

    @Autowired
    public ShellPromptProvider(ContextCommands contextCommands) {
        this.contextCommands = contextCommands;
    }


    @Override
    public String getPrompt() {
        return AbstractShell.shellPrompt =
                AnsiOutput.toString(AnsiColor.CYAN, "hdfs-shell ") +
                        AnsiOutput.toString(AnsiColor.YELLOW, contextCommands.getCurrentDir()) +
                        AnsiOutput.toString(AnsiColor.CYAN, " >", AnsiColor.WHITE);
    }


    @Override
    public String getProviderName() {
        return "Hdfs-shell prompt";
    }

}
