package org.mvnsearch.spring.boot.shell;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vitasek L.
 */
@SuppressWarnings("Duplicates")
public class UnixServer {
    private static final Logger LOG = LoggerFactory.getLogger(UnixServer.class);
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
            LOG.info("server: " + server);

            while (!Thread.interrupted()) {
                LOG.info("Waiting for connection...");
                executorService.execute(new ClientConnection(this, server.accept()));
            }
        } finally {
            executorService.shutdown();
        }
    }


}
