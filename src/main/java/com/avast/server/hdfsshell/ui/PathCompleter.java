package com.avast.server.hdfsshell.ui;

import com.avast.server.hdfsshell.commands.ContextCommands;
import jline.console.completer.Completer;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.JLineLogHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Vitasek L.
 */
@Component
public class PathCompleter implements Completer {

    final
    ContextCommands contextCommands;

    @Autowired
    public PathCompleter(ContextCommands contextCommands) {
        this.contextCommands = contextCommands;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public int complete(final String buffer, final int cursor, final List candidates) {
        int result;
        try {
            JLineLogHandler.cancelRedrawProhibition();
            result = doComplete(buffer, cursor, candidates);
        } finally {
            JLineLogHandler.prohibitRedraw();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private int doComplete(String buffer, int cursor, List candidates) {
        try {
            return analyzePossibilities(buffer, cursor, candidates);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private int analyzePossibilities(String buffer, int cursor, List<String> candidates) throws IOException {
        if (cursor == 0) {
            return -1;
        }
        final String[] args = buffer.substring(0, Math.min(cursor, buffer.length())).split(" |,", 1000);
        if (args.length <= 1) {
            return -1;
        }
        String val = args[args.length - 1];
        if ("".equals(val) || " ".equals(val) || ",".equals(val)) {
            val = ".";
        }

        boolean isAbsolute = val.startsWith("/");

        if (val.startsWith("-")) {
            return -1;
        }

        final int found = val.lastIndexOf('/');
        final String core;
        final String rest;
        if (found == -1) {
            //it's relative
            core = contextCommands.getCurrentDir();
            rest = val;
        } else {
            final String firstPart = val.substring(0, found + 1);
            core = (isAbsolute) ? firstPart : new Path(contextCommands.getCurrentDir(), firstPart).toUri().getPath();
            rest = val.length() == 1 ? "" : val.substring(found + 1);
        }

        final FileSystem fs = FileSystem.get(contextCommands.getConfiguration());
        final Path f = new Path(val);

        boolean folderFound = false;
        if (fs.exists(f) && fs.getFileStatus(f).isDirectory() && !val.endsWith("/") && !val.equals(".")) {
            folderFound = true;
        }

        final Path pathCore = new Path(core);
        if (!fs.exists(pathCore)) {
            return -1;
        }

        final String[] suggestions = getSuggestions(rest, fs, pathCore);

        String commonPrefix = StringUtils.getCommonPrefix(suggestions);
        if (StringUtils.isNotEmpty(commonPrefix) && !commonPrefix.equals(rest)) {
            commonPrefix = rest.isEmpty() || rest.equals(".") ? commonPrefix : commonPrefix.substring(rest.length());
            candidates.add(commonPrefix);
            return cursor;
        } else {
            if (suggestions.length > 1) {
                if (StringUtils.isNotEmpty(commonPrefix) && commonPrefix.equals(rest)) { //problem candidatelistcompletionhandler
                    for (int i = 0; i < suggestions.length; i++) {
                        suggestions[i] = StringUtils.repeat(" ", i) + suggestions[i];
                    }
                }
                candidates.addAll(removeSlash(suggestions));
            } else {
                if (folderFound) {
                    candidates.add("/");
                    return cursor;
                } else {
                    if (suggestions.length > 0) {
                        suggestions[0] = rest.isEmpty() || rest.equals(".") ? suggestions[0] : suggestions[0].substring(rest.length());
                        candidates.addAll(Arrays.asList(suggestions));
                    }
                }
            }
        }


        return suggestions.length == 0 ? -1 : cursor;
    }

    private String[] getSuggestions(String rest, FileSystem fs, Path pathCore) throws IOException {
        final FileStatus[] statuses;
        try {
            statuses = fs.listStatus(pathCore);
        } catch (IOException e) { //not access rights
            return new String[0];
        }
        return Arrays.stream(statuses).
                map(item -> (item.isDirectory()) ? item.getPath().getName() + "/" : item.getPath().getName()).
                filter(item -> (rest.isEmpty() || rest.equals(".")) || (!item.equals(rest) && item.startsWith(rest))).
                toArray(String[]::new);
    }

    private List<String> removeSlash(String[] suggestions) {
        return Arrays.stream(suggestions).map(item -> item.replace("/", "")).collect(Collectors.toList());
    }
}
