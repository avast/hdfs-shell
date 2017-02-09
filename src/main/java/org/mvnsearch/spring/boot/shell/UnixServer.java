package org.mvnsearch.spring.boot.shell;

import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Vitasek L.
 */
@SuppressWarnings("Duplicates")
public class UnixServer {

    public BootShim bootShim;
    private final String path;

    public UnixServer(BootShim bootShim, String path) {
        this.bootShim = bootShim;
        this.path = path;
    }

    public BootShim getBootShim() {
        return bootShim;
    }

    public void setBootShim(BootShim bootShim) {
        this.bootShim = bootShim;
    }

    public void run() throws IOException {
        final File socketFile = new File(path);
        socketFile.deleteOnExit();

        final ExecutorService executorService = Executors.newCachedThreadPool();

        try (AFUNIXServerSocket server = AFUNIXServerSocket.newInstance()) {
            server.bind(new AFUNIXSocketAddress(socketFile));
            System.out.println("server: " + server);

            while (!Thread.interrupted()) {
                System.out.println("Waiting for connection...");
                executorService.execute(new ClientConnection(this, server.accept()));
            }
        } finally {
            executorService.shutdown();
        }
    }


}
