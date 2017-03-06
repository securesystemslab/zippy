/*
 * Copyright (c) 2013, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 package edu.uci.python.runtime;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ZippyEnvVars {

    private static final String ZIPPY_HOME = "ZIPPY_HOME";

    private static String zippyHome = null;

    private static final Properties localEnvVars = new Properties();

    public static String zippyHome() {
        if (zippyHome == null) {
            zippyHome = System.getenv(ZIPPY_HOME);
            Path zippyHomePath;
            if (zippyHome == null) {
                zippyHomePath = getZippyHomePath();
            } else {
                zippyHomePath = Paths.get(zippyHome);
            }
            if (!validateZippyHome(zippyHomePath)) {
                zippyExit("ZIPPY_HOME is not set correctly. please run 'mx set-zippy-home'");
            }
            zippyHome = zippyHomePath.toString();
            try {
                localEnvVars.load(loadZippyEnvVars());
            } catch (FileNotFoundException e) {
                // pass through
            } catch (IOException e) {
                // pass through
            }
        }
        return zippyHome;
    }

    private static Path getZippyHomePath() {
        Path path = Paths.get(ZippyEnvVars.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
        while (path != null) {
            if (validateZippyHome(path)) {
                return path;
            }
            path = path.getParent();
        }
        return path;
    }

    private static boolean validateZippyHome(Path path) {
        if (path == null) {
            return false;
        }
        Path f = path.resolve("LICENSE");
        return Files.exists(f);
    }

    public static RuntimeException zippyExit(String msg) {
        System.err.println("Zippy unexpected failure: " + msg);
        throw new RuntimeException();
    }

    private static FileInputStream loadZippyEnvVars() throws FileNotFoundException, IOException {
        return new FileInputStream(Paths.get(zippyHome, "mx.zippy", "env").toFile());
    }

    private static String libSuffix(String lib) {
        return lib + "." + (isMacOS() ? "dylib" : "so");
    }

    private static boolean isMacOS() {
        String os = System.getProperty("os.name");
        return os.contains("Mac OS");
    }

    public static boolean loadNativeLib(String lib, String envVarName) {
        Path path;

        path = System.getenv(envVarName) != null ? Paths.get(System.getenv(envVarName)) : null;
        if (path != null && Files.exists(path)) {
            System.load(path.toString());
            return true;
        }

        path = Paths.get(localEnvVars.getProperty(envVarName));
        if (path != null && Files.exists(path)) {
            System.load(path.toString());
            return true;
        }

        path = Paths.get(zippyHome, "lib", libSuffix(lib));
        if (path != null && Files.exists(path)) {
            System.load(path.toString());
            return true;
        }

        return false;
    }

}
