/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

/**
 * Downloads content from a given URL to a given file.
 *
 * @param path where to write the content
 * @param urls the URLs to try, stopping after the first successful one
 */
public class URLConnectionDownload {
    /**
     * Iterate over list of environment variable to find one that correctly specify an proxy.
     *
     * @param propPrefix indicates which proxy property to set (i.e., http or https)
     * @param proxyEnvVariableNames list of environment variable
     * @return a string specifying the proxy url
     */
    private static String setProxy(String[] proxyEnvVariableNames, String propPrefix) {
        String proxy = null;
        String proxyEnvVar = "";
        for (String envvar : proxyEnvVariableNames) {
            proxy = System.getenv(envvar);
            if (proxy != null) {
                proxyEnvVar = envvar;
                break;
            }
        }
        if (proxy != null) {
            Pattern p = Pattern.compile("(?:http://)?([^:]+)(:\\d+)?");
            Matcher m = p.matcher(proxy);
            if (m.matches()) {
                String host = m.group(1);
                String port = m.group(2);
                System.setProperty(propPrefix + ".proxyHost", host);
                if (port != null) {
                    port = port.substring(1); // strip ':'
                    System.setProperty(propPrefix + ".proxyPort", port);
                }
                return proxy;
            } else {
                System.err.println("Value of " + proxyEnvVar + " is not valid:  " + proxy);
            }
        } else {
            System.err.println("** If behind a firewall without direct internet access, use the " + proxyEnvVariableNames[0] + "  environment variable (e.g. 'env " + proxyEnvVariableNames[0] +
                            "=proxy.company.com:80 max ...') or download manually with a web browser.");
        }
        return "";
    }

    /**
     * Downloads content from a given URL to a given file.
     *
     * @param args arg[0] is the path where to write the content. The remainder of args are the URLs
     *            to try, stopping after the first successful one
     */
    public static void main(String[] args) {
        File path = new File(args[0]);
        boolean verbose = args[1].equals("-v");
        int offset = verbose ? 2 : 1;
        String[] urls = new String[args.length - offset];
        System.arraycopy(args, offset, urls, 0, urls.length);

        File parent = path.getParentFile();
        makeDirectory(parent);

        // Enable use of system proxies
        System.setProperty("java.net.useSystemProxies", "true");

        // Set standard proxy if any
        String proxy = setProxy(new String[]{"HTTP_PROXY", "http_proxy"}, "http");
        // Set proxy for secure http if explicitely set, default to http proxy otherwise
        String secureProxy = setProxy(new String[]{"HTTPS_PROXY", "https_proxy", "HTTP_PROXY", "http_proxy"}, "https");
        String proxyMsg = "";
        if (secureProxy.length() > 0 && proxy.length() > 0 && !secureProxy.equals(proxy)) {
            proxyMsg = " via " + proxy + " / " + secureProxy;
        } else if (proxy.length() > 0) {
            proxyMsg = " via " + proxy;
        } else if (secureProxy.length() > 0) {
            proxyMsg = " via " + secureProxy;
        }

        for (String s : urls) {
            try {
                while (true) {
                    System.err.println("Downloading " + s + " to  " + path + proxyMsg);
                    URL url = new URL(s);
                    URLConnection conn = url.openConnection();
                    // 10 second timeout to establish connection
                    conn.setConnectTimeout(10000);

                    if (conn instanceof HttpURLConnection) {
                        // HttpURLConnection per default follows redirections,
                        // but not if it changes the protocol (e.g. http ->
                        // https). While this is a sane default, in our
                        // situation it's okay to follow a protocol transition.
                        HttpURLConnection httpconn = (HttpURLConnection) conn;
                        switch (httpconn.getResponseCode()) {
                            case HttpURLConnection.HTTP_MOVED_PERM:
                            case HttpURLConnection.HTTP_MOVED_TEMP:
                                System.err.println("follow redirect...");
                                s = httpconn.getHeaderField("Location");
                                continue;
                        }
                    }
                    InputStream in = conn.getInputStream();
                    int size = conn.getContentLength();
                    FileOutputStream out = new FileOutputStream(path);
                    int read = 0;
                    byte[] buf = new byte[8192];
                    int n = 0;
                    while ((read = in.read(buf)) != -1) {
                        n += read;
                        if (verbose) {
                            long percent = ((long) n * 100 / size);
                            System.err.print("\r " + n + " bytes " + (size == -1 ? "" : " (" + percent + "%)"));
                        }
                        out.write(buf, 0, read);
                    }
                    System.err.println();
                    out.close();
                    in.close();
                    return;
                }
            } catch (MalformedURLException e) {
                throw new Error("Error in URL " + s, e);
            } catch (IOException e) {
                System.err.println("Error reading from  " + s + ":  " + e);
                path.delete();
            }
        }
        throw new Error("Could not download content to  " + path + " from  " + Arrays.toString(urls));
    }

    private static void makeDirectory(File directory) {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new Error("Could not make directory " + directory);
        }
    }
}
