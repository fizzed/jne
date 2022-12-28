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

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class OptionsTest {

    @Test
    public void createResourcePaths() {
        Options options = new Options();
        
        // full path
        assertThat(options.createResourcePaths(OperatingSystem.WINDOWS, HardwareArchitecture.X64, "test"), containsInAnyOrder("/jne/windows/x64/test", "/jne/windows/x86_64/test"));
        assertThat(options.createResourcePaths(OperatingSystem.WINDOWS, HardwareArchitecture.ARM64, "test"), containsInAnyOrder("/jne/windows/arm64/test", "/jne/windows/aarch64/test"));
        assertThat(options.createResourcePaths(OperatingSystem.LINUX, HardwareArchitecture.X64, "test"), containsInAnyOrder("/jne/linux/x64/test", "/jne/linux/x86_64/test"));
        assertThat(options.createResourcePaths(OperatingSystem.LINUX, HardwareArchitecture.X32, "test"), containsInAnyOrder("/jne/linux/x32/test", "/jne/linux/i386/test"));
        assertThat(options.createResourcePaths(OperatingSystem.LINUX, HardwareArchitecture.RISCV64, "test"), containsInAnyOrder("/jne/linux/riscv64/test"));
        assertThat(options.createResourcePaths(OperatingSystem.LINUX, HardwareArchitecture.ARM64, "test"), containsInAnyOrder("/jne/linux/arm64/test", "/jne/linux/aarch64/test"));
        assertThat(options.createResourcePaths(OperatingSystem.MACOS, HardwareArchitecture.X64, "test"), containsInAnyOrder("/jne/macos/x64/test", "/jne/osx/x64/test", "/jne/macos/x86_64/test", "/jne/osx/x86_64/test"));
        assertThat(options.createResourcePaths(OperatingSystem.MACOS, HardwareArchitecture.ARM64, "test"), containsInAnyOrder("/jne/macos/arm64/test", "/jne/osx/arm64/test", "/jne/macos/aarch64/test", "/jne/osx/aarch64/test"));

        // optional arch
        assertThat(options.createResourcePaths(OperatingSystem.WINDOWS, HardwareArchitecture.ANY, "test"), containsInAnyOrder("/jne/windows/test"));
        assertThat(options.createResourcePaths(OperatingSystem.LINUX, HardwareArchitecture.ANY, "test"), containsInAnyOrder("/jne/linux/test"));
        assertThat(options.createResourcePaths(OperatingSystem.MACOS, HardwareArchitecture.ANY, "test"), containsInAnyOrder("/jne/macos/test", "/jne/osx/test"));
        
        // optional os
        assertThat(options.createResourcePaths(OperatingSystem.ANY, HardwareArchitecture.ANY, "test"), containsInAnyOrder("/jne/test"));
    }
    
}
