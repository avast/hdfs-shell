package org.mvnsearch.spring.boot.shell;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.shell.CommandLine;
import org.springframework.shell.ShellException;
import org.springframework.shell.SimpleShellCommandLineOptions;
import org.springframework.shell.core.ExitShellRequest;
import org.springframework.shell.core.JLineShellComponent;
import org.springframework.util.StopWatch;

/**
 * Spring Boot Shim
 *
 * @author jeffellin
 * @author linux_china
 */
public class BootShim {
    private static final Logger LOG = LoggerFactory.getLogger(BootShim.class);
    private static StopWatch sw = new StopWatch("Spring Shell");
    private static CommandLine commandLine;
    private ConfigurableApplicationContext ctx;

    public BootShim(String[] args, ConfigurableApplicationContext context) {
        this.ctx = context;
        try {
            commandLine = SimpleShellCommandLineOptions.parseCommandLine(args);
        } catch (IOException var5) {
            throw new ShellException(var5.getMessage(), var5);
        }
        this.configureApplicationContext(this.ctx);
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner((BeanDefinitionRegistry) this.ctx);
        if (commandLine.getDisableInternalCommands()) {
            scanner.scan("org.springframework.shell.converters", "org.springframework.shell.plugin.support");
        } else {
            scanner.scan("org.springframework.shell.commands", "org.springframework.shell.converters", "org.springframework.shell.plugin.support");
        }
    }

    private void configureApplicationContext(ConfigurableApplicationContext annctx) {
        this.createAndRegisterBeanDefinition(annctx, JLineShellComponent.class, "shell");
        annctx.getBeanFactory().registerSingleton("commandLine", commandLine);
    }

    private void createAndRegisterBeanDefinition(ConfigurableApplicationContext annctx, Class<?> clazz, String name) {
        RootBeanDefinition rbd = new RootBeanDefinition();
        rbd.setBeanClass(clazz);
        DefaultListableBeanFactory bf = (DefaultListableBeanFactory) annctx.getBeanFactory();
        if (name != null) {
            bf.registerBeanDefinition(name, rbd);
        } else {
            bf.registerBeanDefinition(clazz.getSimpleName(), rbd);
        }

    }


    public JLineShellComponent getJLineShellComponent() {
        return ctx.getBean("shell", JLineShellComponent.class);
    }


    public ExitShellRequest run() throws IllegalAccessException {
        sw.start();
        String[] commandsToExecuteAndThenQuit = commandLine.getShellCommandsToExecute();
        JLineShellComponent shell = this.ctx.getBean("shell", JLineShellComponent.class);
        ExitShellRequest exitShellRequest;
        if (null != commandsToExecuteAndThenQuit) {
            boolean successful = false;
            exitShellRequest = ExitShellRequest.FATAL_EXIT;
            for (String cmd : commandsToExecuteAndThenQuit) {
                successful = shell.executeCommand(cmd).isSuccess();
                if (!successful) {
                    break;
                }
            }
            if (successful) {
                exitShellRequest = ExitShellRequest.NORMAL_EXIT;
            }
        } else {
            shell.start();
            shell.promptLoop();
            exitShellRequest = shell.getExitShellRequest();
            if (exitShellRequest == null) {
                exitShellRequest = ExitShellRequest.NORMAL_EXIT;
            }
            shell.waitForComplete();
        }
        sw.stop();
        if (shell.isDevelopmentMode()) {
            LOG.info("Total execution time: " + sw.getLastTaskTimeMillis() + " ms");
        }
        return exitShellRequest;
    }


    public ConfigurableApplicationContext getCtx() {
        return ctx;
    }
}
