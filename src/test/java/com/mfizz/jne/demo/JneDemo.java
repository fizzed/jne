package com.mfizz.jne.demo;

/*
 * #%L
 * mfz-ffmpeg
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

import com.mfizz.jne.JNE;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class JneDemo {
    private static final Logger logger = LoggerFactory.getLogger(JneDemo.class);
    
    static public void main(String[] args) throws Exception {
        
        // use same directory between app runs!
        //File extractDir = new File(System.getProperty("java.io.tmpdir"), "jnetemp");
        //extractDir.mkdirs();
        //File catExeFile = JNE.find("cat", extractDir, false);
        
        // use one-time use temporary directory
        File catExeFile = JNE.find("cat");
        
        if (catExeFile == null) {
            logger.error("Unable to find executable!");
            System.exit(1);
        }
        
        logger.info("java version: " + System.getProperty("java.version"));
        logger.info("java home: " + System.getProperty("java.home"));
        logger.info("using exe: " + catExeFile.getAbsolutePath());
        
        // use "cat" to print out an expected file
        File expectedTxtFile = new File(JneDemo.class.getResource("/test.txt").toURI());
        File actualTxtFile = new File("target", "actual.txt");
        
        ProcessBuilder pb = new ProcessBuilder(catExeFile.getAbsolutePath(), expectedTxtFile.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        int retVal = p.waitFor();
        
        logger.info("ret val: " + retVal);
    }
    
}
