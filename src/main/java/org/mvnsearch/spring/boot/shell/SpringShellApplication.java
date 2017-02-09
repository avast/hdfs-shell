package org.mvnsearch.spring.boot.shell;

import com.avast.server.hdfsshell.commands.ContextCommands;
import com.avast.server.hdfsshell.ui.PathCompleter;
import jline.console.ConsoleReader;
import org.springframework.boot.Banner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.shell.core.CommandResult;
import org.springframework.shell.core.JLineShellComponent;
import org.springframework.shell.support.logging.HandlerUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * spring shell application
 *
 * @author linux_china
 * @author Vitasek
 */
public class SpringShellApplication {

    public static int run(Object source, String... args) {
        return run(new Object[]{source}, args);
    }

    public static int run(Object[] sources, String[] args) {
        final SpringApplication springApplication = new SpringApplication(sources);
        if (args.length > 0 || System.getenv("HDFS_SHELL_NO_BANNER") != null) {
            springApplication.setBannerMode(Banner.Mode.OFF);
        }
        // disable hardcoded loggers to FINE level
        ConfigurableApplicationContext ctx = springApplication.run(args);
        Handler[] handlers = Logger.getLogger( "" ).getHandlers();
        for (Handler handler : handlers) {
            handler.setLevel(Level.OFF);
        }
        try {
            final BootShim bootShim = new BootShim(args, ctx);
            if (args.length > 0) {
                JLineShellComponent shell = bootShim.getJLineShellComponent();
                final String command = Arrays.stream(args).collect(Collectors.joining(" "));
                if (args[0].equals("script")) {
                    final ContextCommands context = ctx.getBean(ContextCommands.class);
                    context.setFailOnError(true);
                }

                final CommandResult commandResult;
                try {
                    commandResult = shell.executeCommand(command);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    return SpringApplication.exit(ctx, (ExitCodeGenerator) () -> -1);
                }
                if (!commandResult.isSuccess()) {
                    return SpringApplication.exit(ctx, (ExitCodeGenerator) () -> -1);
                } else {
                    if (commandResult.getResult() != null) {
                        System.out.println(commandResult.getResult());
                    }
                }
            } else {
                if (System.getProperty("daemon") != null) {
                    final Environment env = ctx.getBean(Environment.class);
                    new UnixServer(bootShim, env.getProperty("socket.filepath", "/var/tmp/hdfs-shell.sock")).run();
                } else {
                    Runtime.getRuntime().addShutdownHook(new Thread(System.out::println));//another new line on exit from interactive mode
                    new Timer().schedule(new InitCompletionTimerTask(bootShim), 5000);// hack
                    bootShim.run();
                }
            }
        } catch (IllegalAccessException | IOException e) {
            e.printStackTrace();
            return -1;
        } finally {
            HandlerUtils.flushAllHandlers(Logger.getLogger(""));
        }
        return 0;
    }

    private static class InitCompletionTimerTask extends TimerTask {
        private final BootShim bootShim;

        public InitCompletionTimerTask(BootShim bootShim) {
            this.bootShim = bootShim;
        }

        @Override
        public void run() {
            JLineShellComponent shell = bootShim.getJLineShellComponent();
            final Field readerField = ReflectionUtils.findField(JLineShellComponent.class, "reader");
            ReflectionUtils.makeAccessible(readerField);
            final ConsoleReader reader;
            try {
                reader = (ConsoleReader) readerField.get(shell);
                if (reader == null) {
                    new Timer().schedule(new InitCompletionTimerTask(bootShim), 5000);
                    return;
                }
                reader.addCompleter(new PathCompleter(bootShim.getCtx().getBean(ContextCommands.class)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
