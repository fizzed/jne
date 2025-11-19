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

import helloj.HelloLib;
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

public class LibTest {
    static private final Logger log = LoggerFactory.getLogger(LibTest.class);

    @Test
    public void findLibrary() throws Exception {
        // use one-time use temporary directory
        final File libraryFile = JNE.findLibrary("helloj");

        assertThat("Unable to find LIB 'helloj' in resources (likely means its not compiled for this platform/os yet?)",
            libraryFile, is(not(nullValue())));
    }

    @Test
    public void loadLibrary() throws Exception {
        // leverage JNE loader to load up the helloj lib
        LibLoader.loadLibrary();

        // now use it
        HelloLib helloLib = new HelloLib();
        final String s = helloLib.hi();

        assertThat(s, is("Hello from JNI!"));
    }
    
}
