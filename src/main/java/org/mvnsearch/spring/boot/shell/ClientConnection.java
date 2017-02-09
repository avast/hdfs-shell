package org.mvnsearch.spring.boot.shell;

import org.springframework.shell.core.CommandResult;
import org.springframework.shell.core.JLineShellComponent;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Vitasek L.
 */
public class ClientConnection implements Runnable {

    public static final ThreadLocal<PrintStream> context = new ThreadLocal<>();

    private Socket sock;
    private final JLineShellComponent shell;

    public ClientConnection(UnixServer unixServer, Socket sock) {
        this.sock = sock;
        shell = unixServer.getBootShim().getJLineShellComponent();
    }

    @Override
    public void run() {
        context.remove();

        try (Scanner is = new Scanner(sock.getInputStream()); OutputStream os = sock.getOutputStream()) {
            System.out.println("Connected: " + sock);

            try (PrintStream writer = new PrintStream(os, true)) {
                context.set(writer);

                String command;
                while (is.hasNextLine() && (command = is.nextLine()) != null) {

                    final CommandResult commandResult = shell.executeCommand(command);
                    if (!commandResult.isSuccess()) {
                        System.err.println(commandResult.getException().getMessage());
                        writer.println(commandResult.getException().getMessage());
                    } else {
                        if (commandResult.getResult() != null) {
                            writer.println(commandResult.getResult().toString());
                        }
                    }
                    writer.flush();

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            context.remove();
        }

    }

}
