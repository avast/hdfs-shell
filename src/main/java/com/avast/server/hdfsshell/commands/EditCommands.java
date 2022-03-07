package com.avast.server.hdfsshell.commands;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.avast.server.hdfsshell.utils.BashUtils;

/**
 * @author Vitasek L.
 */
@Component
public class EditCommands implements CommandMarker {
    private static final Logger LOG = LoggerFactory.getLogger(EditCommands.class);

    final HadoopDfsCommands hadoopDfsCommands;
    private final ContextCommands contextCommands;


    @Value("${EDITOR:}")
    String editor;

    @Autowired
    public EditCommands(HadoopDfsCommands hadoopDfsCommands, ContextCommands contextCommands) {
        this.hadoopDfsCommands = hadoopDfsCommands;
        this.contextCommands = contextCommands;
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @CliCommand(value = "edit", help = "Get file to local file system, edit and put it back to HDFS")
    public String set(@CliOption(key = {""}, help = "File to edit") String path) throws IOException {
        if (StringUtils.isEmpty(path)) {
            return "You have to define path param";
        }
        Path p = getFilePathForEdit(path);

        if (!contextCommands.getFileSystem().exists(p)) {
            return "Path " + p + " does not exists. Invalid file?";
        }

        final File localTempFile = getLocalTempFile(p.getName());
        try {
            final String getCommandResult = hadoopDfsCommands.runCommand("get", new String[]{p.toString(), localTempFile.getAbsolutePath()});
            if (StringUtils.isEmpty(getCommandResult)) {
                if (editFile(localTempFile)) {
                    final String putCommandResult = hadoopDfsCommands.runCommand("put", new String[]{"-f", localTempFile.getAbsolutePath(), p.toString()});
                    if (StringUtils.isEmpty(putCommandResult)) {
                        LOG.info("File {} was updated successfully", p.getName());
                        return "File " + p.getName() + " was updated succesfully.";
                    }
                } else {
                    return "File " + p.getName() + " was NOT updated.";
                }
            }

        } catch (Exception e) {
            return "Failed to edit file: " + e.getMessage();
        } finally {
            localTempFile.delete();
        }


        return "";
    }

    private Path getFilePathForEdit(@CliOption(key = {""}, help = "File to edit") String path) {
        Path p;
        if (!path.startsWith("/")) {
            p = new Path(contextCommands.getCurrentDir(), path);
        } else {
            p = new Path(path);
        }
        return p;
    }

    private File getLocalTempFile(String filename) {
        return new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString().substring(0, 10) + "-" + filename);
    }


    public boolean editFile(final File file) throws IOException, InterruptedException {
        final long lastModified = file.lastModified();
        final String[] editor = getEditor();
        if (editor.length == 0) {
            LOG.info("No editor is defined");
            return false;
        }

        final ProcessBuilder pb = new ProcessBuilder(ArrayUtils.addAll(editor, file.getAbsolutePath()));
        // Merge System.err and System.out
        pb.redirectErrorStream(true);
        // Inherit System.out as redirect output stream
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        LOG.info("Launching command {}", pb.command().stream().collect(Collectors.joining(" ")));

        final Process process = pb.start();
        final int resultCode = process.waitFor();
        return resultCode == 0 && lastModified != file.lastModified();
    }

    public String[] getEditor() {
        if (StringUtils.isEmpty(this.editor)) {
            if (SystemUtils.IS_OS_WINDOWS) {
                return new String[]{"notepad.exe"};
            }
            if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
                return new String[]{"vim"};
            }
            return new String[]{"vim"};
        } else {
            return BashUtils.parseArguments(this.editor);
        }
    }

}
