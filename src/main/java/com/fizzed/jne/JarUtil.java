package com.fizzed.jne;

/*
 * #%L
 * jne
 * %%
 * Copyright (C) 2015 Fizzed, Inc
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Utilities for working with jar files.
 * 
 * @author joelauer
 */
public class JarUtil {

    static private final Logger log = LoggerFactory.getLogger(JarUtil.class);

    
    static public File getJarFileForResource(URL resource) throws IOException {
        if (!resource.getProtocol().equalsIgnoreCase("jar")) {
            throw new IOException("Resource protocol was not jar");
        }
        
        // e.g. file:/home/joelauer/.m2/repository/com/mfizz/mfz-jne-cat/1.0.0-SNAPSHOT/mfz-jne-cat-1.0.0-SNAPSHOT.jar!/jne/linux/x64/cat
        String file = resource.getFile();
        int exclaimPos = file.indexOf("!");
        if (exclaimPos < 0) {
            throw new IOException("Missing ! char (invalid jar resource)");
        }
        
        String tempJarFile = file.substring(0, exclaimPos);
        log.debug("tempJarFile: " + tempJarFile);
        
        try {
            File jarFile = new File(new URL(tempJarFile).toURI());
            //System.out.println("jarFile: " + jarFile);
            //System.out.println("exists? " + jarFile.exists());
            return jarFile;
        } catch (MalformedURLException e) {
            throw new IOException("Unable to create uri for jar file", e);
        } catch (URISyntaxException e) {
            throw new IOException("Unable to create uri for jar file", e);
        }
    }
    
    // e.g. /drivers/h2/h2-1.3.162.jar
    static public String getManifestVersionNumber(File file) throws IOException {
        JarFile jar = new JarFile(file);
        Manifest manifest = jar.getManifest();
        String versionNumber = null;
        java.util.jar.Attributes attributes = manifest.getMainAttributes();
        if (attributes!=null) {
            Iterator it = attributes.keySet().iterator();
            while (it.hasNext()) {
                Attributes.Name key = (Attributes.Name)it.next();
                String keyword = key.toString();
                if (keyword.equals("Implementation-Version") || keyword.equals("Bundle-Version")){
                    versionNumber = (String)attributes.get(key);
                    break;
                }
            }
        }
        jar.close();
        
        if (versionNumber == null || versionNumber.equals("")) {
            return null;
        }

        return versionNumber;
    }
    
    /**
    static public String getFileVersionNumber(File file) {
        String versionNumber = null;
        String fileNameNoExt = file.getName().substring(0, file.getName().lastIndexOf("."));
        if (fileName.contains(".")) {
            // version will be from last "-" to end
            int delimiter = fileName.lastIndexOf("-");
            
            String majorVersion = fileName.substring(0, fileName.indexOf("."));
            String minorVersion = fileName.substring(fileName.indexOf("."));
            
            if (majorVersion.indexOf("_")>delimiter) delimiter = majorVersion.indexOf("_");
            majorVersion = majorVersion.substring(delimiter+1, fileName.indexOf("."));
            versionNumber = majorVersion + minorVersion;
        }
        System.out.println("Version: " + versionNumber); //"Version: 1.3.162"
    }
    */
    
}
