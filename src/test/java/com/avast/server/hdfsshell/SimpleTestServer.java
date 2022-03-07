package com.avast.server.hdfsshell;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vitasek L.
 */
public class SimpleTestServer {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleTestServer.class);
    public static void main(String[] args) throws IOException {
        final File socketFile =
                new File(new File(System.getProperty("java.io.tmpdir")), "junixsocket-test.sock");

        try (AFUNIXServerSocket server = AFUNIXServerSocket.newInstance()) {
            server.bind(new AFUNIXSocketAddress(socketFile));
            LOG.info("server: " + server);

            while (!Thread.interrupted()) {
                LOG.info("Waiting for connection...");
                try (Socket sock = server.accept()) {
                    LOG.info("Connected: " + sock);

                    try (InputStream is = sock.getInputStream(); OutputStream os = sock.getOutputStream()) {
                        byte[] buf = new byte[128];
                        int read = is.read(buf);
                        LOG.info("Client's response: " + new String(buf, 0, read));

                        LOG.info("Saying hello to client " + os);
                        os.write("Hello, dear Client".getBytes());
                        os.flush();

                    }
                }
            }
        }
    }
}
