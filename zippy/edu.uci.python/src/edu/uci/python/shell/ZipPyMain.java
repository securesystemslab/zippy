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
package edu.uci.python.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.python.core.Py;
import org.python.core.PyFile;
import org.python.core.util.RelativeFile;
import org.python.modules.posix.PosixModule;
import org.python.util.InteractiveConsole;

public class ZipPyMain {

    public static void main(String[] args) {
        if (args.length < 1 || args[0].equals("-h") || args[0].equals("--help")) {
            System.out.println("Please run 'mx python --help' for more information.");
            if (args.length == 0)
                System.out.println("Interactive shell is not supported yet.");
        } else {
            String scriptName = args[0];
            File script = new File(scriptName);
            if (script.isFile()) {
                @SuppressWarnings("unused")
                String path = null;
                try {
                    path = script.getCanonicalFile().getParent();
                } catch (IOException ioe) {
                    path = script.getAbsoluteFile().getParent();
                }

                InteractiveConsole interp = ZipPyConsole.newInterpreter(args, null, true);
                try {
                    FileInputStream file;

                    try {
                        file = new FileInputStream(new RelativeFile(scriptName));
                    } catch (FileNotFoundException e) {
                        throw Py.IOError(e);
                    }

                    try {
                        if (PosixModule.getPOSIX().isatty(file.getFD())) {
                            interp.interact(null, new PyFile(file));
                            return;
                        } else {
                            interp.execfile(file, scriptName);
                        }
                    } finally {
                        file.close();
                    }
                } catch (Throwable t) {
                    ZipPyConsole.dispose(interp, t, true);
                }
            }
        }
    }

}
