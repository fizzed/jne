package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2022 Fizzed, Inc
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

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExeTest {
    static private final Logger log = LoggerFactory.getLogger(ExeTest.class);

    @Test
    public void cat() throws Exception {
        log.info("java version: " + System.getProperty("java.version"));
        log.info("java home: " + System.getProperty("java.home"));

        // use one-time use temporary directory
        final File catExeFile = JNE.findExecutable("jcat");

        assertThat("Unable to find 'jcat' in resources (likely means its not compiled for this platform/os yet?)", catExeFile, is(not(nullValue())));

        // use "cat" to print out an expected file
        final Path expectedTxtFile = new File(JneDemo.class.getResource("/test.txt").toURI()).toPath();
        final Path actualTxtFile = Paths.get("target", "actual.txt");

        if (Files.exists(actualTxtFile)) {
            Files.delete(actualTxtFile);
        }

        final int exitValue = new ProcessExecutor()
            .command(catExeFile.getAbsolutePath().toString(), expectedTxtFile.toAbsolutePath().toString())
            .execute()
            .getExitValue();

        assertThat(exitValue, is(0));
    }
    
}
