package com.avast.server.hdfsshell.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.shell.Command;
import org.apache.hadoop.fs.shell.CommandFactory;
import org.apache.hadoop.util.ReflectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vitasek L.
 */
public class RealCommandFactory extends CommandFactory {
    private Map<String, Class<? extends Command>> classMap =
            new HashMap<String, Class<? extends Command>>();


    public RealCommandFactory() {
        super();
    }

    public RealCommandFactory(Configuration conf) {
        super(conf);
    }

    /**
     * Register the given class as handling the given list of command
     * names.
     *
     * @param cmdClass the class implementing the command names
     * @param names    one or more command names that will invoke this class
     */
    public void addClass(Class<? extends Command> cmdClass, String... names) {
        for (String name : names) classMap.put(name, cmdClass);
    }

    @Override
    public Command getInstance(String cmdName, Configuration conf) {
        if (conf == null) throw new NullPointerException("configuration is null");
        Class<? extends Command> cmdClass = classMap.get(cmdName);
        if (cmdClass != null) {
            Command instance = ReflectionUtils.newInstance(cmdClass, conf);
            instance.setName(cmdName);
        }
        return null;
    }
}
