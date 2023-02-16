package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2017 Fizzed, Inc
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

import java.io.File;
import java.io.IOException;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class JNETest {

    @Test
    public void findFile() throws IOException, ExtractException {
        Options options = new Options();
        
        // specific os & arch
        options.setOperatingSystem(OperatingSystem.LINUX);
        options.setHardwareArchitecture(HardwareArchitecture.X64);
        options.setLinuxLibC(null);
        
        File file;
        
        file = JNE.findFile("resource-linux-x64.txt", options);
        assertThat(file, is(not(nullValue())));
        
        // should fallback to skipping arch
        file = JNE.findFile("resource-linux.txt", options);
        assertThat(file, is(not(nullValue())));
        
        // should fallback to skipping arch & os
        file = JNE.findFile("resource.txt", options);
        assertThat(file, is(not(nullValue())));
        
        // does not exist
        file = JNE.findFile("does-not-exist.txt", options);
        assertThat(file, is(nullValue()));
    }
    
    @Test
    public void findExecutable() throws IOException, ExtractException {
        Options options = new Options();
        
        // specific os & arch
        options.setOperatingSystem(OperatingSystem.LINUX);
        options.setHardwareArchitecture(HardwareArchitecture.X64);
        options.setLinuxLibC(null);
        
        File file;
        
        file = JNE.findExecutable("jcat", options);
        assertThat(file, is(not(nullValue())));
        
        // does not exist
        file = JNE.findExecutable("does-not-exist", options);
        assertThat(file, is(nullValue()));
        
        options.setOperatingSystem(OperatingSystem.WINDOWS);
        
        // .exe should be added
        file = JNE.findExecutable("jcat", options);
        assertThat(file, is(not(nullValue())));
        assertThat(file.getName(), endsWith(".exe"));
    }
    
}
