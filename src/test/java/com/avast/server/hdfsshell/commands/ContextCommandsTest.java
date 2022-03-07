package com.avast.server.hdfsshell.commands;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.shell.CommandFactory;
import org.apache.hadoop.fs.shell.FsCommand;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.Bootstrap;
import org.springframework.shell.core.CommandResult;
import org.springframework.shell.core.JLineShellComponent;

/**
 * @author Vitasek L.
 */
public class ContextCommandsTest {
    private static final Logger LOG = LoggerFactory.getLogger(ContextCommandsTest.class);
    public void exists() throws Exception {
        Bootstrap bootstrap = new Bootstrap();

        JLineShellComponent shell = bootstrap.getJLineShellComponent();

        CommandResult cr = shell.executeCommand("exists /analytics");
        assertEquals(true, cr.isSuccess());
    }

    @Test
    @Ignore
    public void generateMethods() {
        final CommandFactory commandFactory = new CommandFactory(new Configuration());
        FsCommand.registerCommands(commandFactory);
        final String[] names = commandFactory.getNames();
        final String collect = Arrays.stream(names).map(item -> "\"" + item.replace("-", "") + "\"").collect(Collectors.joining(","));
        LOG.info(collect);
        Arrays.stream(names).map(commandFactory::getInstance).forEach(item -> {
            String description = "";
            final String[] sentences = item.getDescription().split("\\.");
            if (sentences.length == 0) {
                description = item.getDescription();
            } else {
                description = sentences[0] + ".";
            }


            String cliCommand = String.format("@CliCommand(value = {\"%s\", \"hdfs dfs -%s\"}, help = \"%s\")", item.getCommandName(), item.getCommandName(), description);
            String content = String.format("    public String %s(\n" +
                    "            @CliOption(key = {\"\"}, help = \"%s\") String path\n" +
                    "    ) {\n" +
                    "        return runCommand(\"%s\", path);\n" +
                    "    }\n", item.getCommandName(), description, item.getCommandName());

            LOG.info(cliCommand);
            LOG.info(content);
        });

    }


}