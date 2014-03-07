/*
 * Copyright 2014 mfizz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mfizz.jne;

/*
 * #%L
 * mfz-jne
 * %%
 * Copyright (C) 2012 - 2014 mfizz
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 *
 * @author joelauer
 */
public class JNE {
    
    private static final int TEMP_DIR_ATTEMPTS = 100;
    private static File _tempDir;
 
    /**
     * Finds (or extracts) a named executable for the runtime operating system
     * and architecture.
     * @param name The executable name you would normally type on the command-line.
     *      For example, "cat" or "ping" would search for "ping.exe" on windows and "ping" on linux/mac.
     * @return The executable file or null if no executable found.
     * @throws NativeExecutableException Thrown if a runtime exception occurs while
     *      finding or extracting the executable.
     */
    synchronized static public File find(String name) throws NativeExecutableException {
        // build path to resource
        OS os = OS.getOS();
        Arch arch = Arch.getArch();
        
        if (os == null || os == OS.UNKNOWN) {
            throw new NativeExecutableException("Unable to detect operating system (e.g. Windows)");
        }
        
        if (arch == null || arch == Arch.UNKNOWN) {
            throw new NativeExecutableException("Unable to detect hardware architecture (e.g. x86)");
        }
        
        // adjust executable name for windows
        String exeName = name;
        if (os == OS.WINDOWS) {
            exeName += ".exe";
        }
        
        String resourcePath = "/jne/" + os.name().toLowerCase() + "/" + arch.name().toLowerCase() + "/" + exeName;
    
        URL url = JNE.class.getResource(resourcePath);
        if (url == null) {
            return null;
        }
        
        // support for "file" and "jar"
        if (url.getProtocol().equals("jar")) {
            // we will need to extract the executable file
            File tempDir = getOrCreateTempDirectory();
            File exeFile = new File(tempDir, exeName);
            // does exe already exist? (previously extracted)
            if (!exeFile.exists()) {
                try {
                    extractTo(url, exeFile);
                    // schedule this extract file for deletion
                    exeFile.deleteOnExit();
                    // set file to "executable"
                    exeFile.setExecutable(true);
                } catch (IOException e) {
                    throw new NativeExecutableException("Unable to cleanly extract executable from jar", e);
                }
            }
            // is it executable?
            return exeFile;
        } else {
            throw new NativeExecutableException("Unsupported executable resource protocol [" + url.getProtocol() + "]");
        }
    }
    
    static private void extractTo(URL url, File file) throws IOException {
        final InputStream in = url.openStream();
        try {
            final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            try {
                int len;
                byte[] buffer = new byte[8192];
                while ((len = in.read(buffer)) > -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    /**
     * Attempts to create a temporary directory that did not exist previously.
     */
    synchronized static private File getOrCreateTempDirectory() throws NativeExecutableException {
        // return the single instance if already created
        if (_tempDir != null) {
            return _tempDir;
        }
        
	File baseDir = new File(System.getProperty("java.io.tmpdir"));
	String baseName = System.currentTimeMillis() + "-";
	
	for (int counter = 0; counter < 100; counter++) {
	    File d = new File(baseDir, baseName + counter);
	    if (d.mkdir()) {
                // schedule this directory to be deleted on exit
                d.deleteOnExit();
                _tempDir = d;
		return d;
	    }
	}
        
	throw new NativeExecutableException("Failed to create temporary directory within " + TEMP_DIR_ATTEMPTS + " attempts (tried " + baseName + "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')');
    }
    
}
