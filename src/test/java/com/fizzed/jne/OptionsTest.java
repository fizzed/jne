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

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OptionsTest {

    @Test
    public void createResourcePaths() {
        Options options = new Options();

        assertThat(options.createResourcePaths(OperatingSystem.WINDOWS, HardwareArchitecture.X64, null, "test"),
                is(asList("/jne/windows/x64/test", "/jne/windows/x86_64/test", "/jne/windows/amd64/test")));

        // ignore glibc
        assertThat(options.createResourcePaths(OperatingSystem.LINUX, HardwareArchitecture.X64, LinuxLibC.GLIBC, "test"),
                is(asList("/jne/linux/x64/test", "/jne/linux/x86_64/test", "/jne/linux/amd64/test")));

        // musl (e.g. alpine)
        assertThat(options.createResourcePaths(OperatingSystem.LINUX, HardwareArchitecture.X64, LinuxLibC.MUSL, "test"),
                is(asList("/jne/linux_musl/x64/test", "/jne/linux_musl/x86_64/test", "/jne/linux_musl/amd64/test")));

        assertThat(options.createResourcePaths(OperatingSystem.WINDOWS, HardwareArchitecture.ARM64, null, "test"),
                is(asList("/jne/windows/arm64/test", "/jne/windows/aarch64/test")));

        assertThat(options.createResourcePaths(OperatingSystem.LINUX, HardwareArchitecture.X64, null, "test"),
                is(asList("/jne/linux/x64/test", "/jne/linux/x86_64/test", "/jne/linux/amd64/test")));

        assertThat(options.createResourcePaths(OperatingSystem.LINUX, HardwareArchitecture.X32, null, "test"),
                is(asList("/jne/linux/x32/test", "/jne/linux/i386/test", "/jne/linux/i586/test", "/jne/linux/i686/test")));

        assertThat(options.createResourcePaths(OperatingSystem.LINUX, HardwareArchitecture.RISCV64, null, "test"),
                is(asList("/jne/linux/riscv64/test")));

        assertThat(options.createResourcePaths(OperatingSystem.LINUX, HardwareArchitecture.ARM64, null, "test"),
                is(asList("/jne/linux/arm64/test", "/jne/linux/aarch64/test")));

        assertThat(options.createResourcePaths(OperatingSystem.MACOS, HardwareArchitecture.X64, null, "test"),
                is(asList("/jne/macos/x64/test", "/jne/macos/x86_64/test", "/jne/macos/amd64/test", "/jne/osx/x64/test", "/jne/osx/x86_64/test", "/jne/osx/amd64/test")));

        assertThat(options.createResourcePaths(OperatingSystem.MACOS, HardwareArchitecture.ARM64, null, "test"),
                is(asList("/jne/macos/arm64/test", "/jne/macos/aarch64/test", "/jne/osx/arm64/test", "/jne/osx/aarch64/test")));

        // optional arch
        assertThat(options.createResourcePaths(OperatingSystem.WINDOWS, HardwareArchitecture.ANY, null, "test"),
                is(asList("/jne/windows/test")));

        assertThat(options.createResourcePaths(OperatingSystem.LINUX, HardwareArchitecture.ANY, null, "test"),
                is(asList("/jne/linux/test")));

        assertThat(options.createResourcePaths(OperatingSystem.MACOS, HardwareArchitecture.ANY, null, "test"),
                is(asList("/jne/macos/test", "/jne/osx/test")));
        
        // optional os
        assertThat(options.createResourcePaths(OperatingSystem.ANY, HardwareArchitecture.ANY, null, "test"),
                is(asList("/jne/test")));
    }
    
}
