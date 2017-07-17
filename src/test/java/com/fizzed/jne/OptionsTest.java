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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class OptionsTest {

    @Test
    public void createResourcePath() {
        Options options = new Options();
        
        // full path
        assertThat(options.createResourcePath(OperatingSystem.WINDOWS, HardwareArchitecture.X64, "test"), is("/jne/windows/x64/test"));
        assertThat(options.createResourcePath(OperatingSystem.LINUX, HardwareArchitecture.X64, "test"), is("/jne/linux/x64/test"));
        assertThat(options.createResourcePath(OperatingSystem.OSX, HardwareArchitecture.X64, "test"), is("/jne/osx/x64/test"));
        
        // optional arch
        assertThat(options.createResourcePath(OperatingSystem.WINDOWS, HardwareArchitecture.ANY, "test"), is("/jne/windows/test"));
        assertThat(options.createResourcePath(OperatingSystem.LINUX, HardwareArchitecture.ANY, "test"), is("/jne/linux/test"));
        assertThat(options.createResourcePath(OperatingSystem.OSX, HardwareArchitecture.ANY, "test"), is("/jne/osx/test"));
        
        // optional os
        assertThat(options.createResourcePath(OperatingSystem.ANY, HardwareArchitecture.ANY, "test"), is("/jne/test"));
    }
    
}
