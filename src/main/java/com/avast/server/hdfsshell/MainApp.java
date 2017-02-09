package com.avast.server.hdfsshell;

import org.mvnsearch.spring.boot.shell.SpringShellApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Vitasek L.
 */

@SpringBootApplication
public class MainApp {

    public static void main(String[] args) {
        System.exit(SpringShellApplication.run(MainApp.class, args));
    }
}
