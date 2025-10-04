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
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class PlatformInfoTest {
    static private final Logger log = LoggerFactory.getLogger(PlatformInfoTest.class);

    @Test
    public void doDetectOperatingSystem() {
        OperatingSystem operatingSystem = PlatformInfo.doDetectOperatingSystem();

        assertThat(operatingSystem, is(not(nullValue())));
//        assertThat(operatingSystem, is(not(OperatingSystem.UNKNOWN)));
    }

    @Test
    public void detectOperatingSystemFromValues() {
        assertThat(PlatformInfo.detectOperatingSystemFromValues("Windows"), is(OperatingSystem.WINDOWS));
        assertThat(PlatformInfo.detectOperatingSystemFromValues("Windows Server 2022"), is(OperatingSystem.WINDOWS));
        assertThat(PlatformInfo.detectOperatingSystemFromValues("Mac OS X"), is(OperatingSystem.MACOS));
        assertThat(PlatformInfo.detectOperatingSystemFromValues("SunOS"), is(OperatingSystem.SOLARIS));
        assertThat(PlatformInfo.detectOperatingSystemFromValues("blah"), is(nullValue()));
        assertThat(PlatformInfo.detectOperatingSystemFromValues(null), is(nullValue()));
    }

    @Test
    public void doDetectHardwareArchitecture() {
        log.debug("=============================== Real Arch Test ===============================================");
        HardwareArchitecture hardwareArchitecture = PlatformInfo.doDetectHardwareArchitecture();

        assertThat(hardwareArchitecture, is(not(nullValue())));
//        assertThat(hardwareArchitecture, is(not(HardwareArchitecture.UNKNOWN)));

        log.debug("==============================================================================================");
    }

    @Test
    public void detectHardwareArchitectureFromValues() {
        assertThat(PlatformInfo.detectHardwareArchitectureFromValues("amd64", null, null, null), is(HardwareArchitecture.X64));
        assertThat(PlatformInfo.detectHardwareArchitectureFromValues("blah", null, null, null), is(nullValue()));
        assertThat(PlatformInfo.detectHardwareArchitectureFromValues("arm", null, null, null), is(nullValue()));
        assertThat(PlatformInfo.detectHardwareArchitectureFromValues("arm", "gnueabihf", null, null), is(HardwareArchitecture.ARMHF));
        assertThat(PlatformInfo.detectHardwareArchitectureFromValues("arm", "gnueabi", null, null), is(HardwareArchitecture.ARMEL));
        assertThat(PlatformInfo.detectHardwareArchitectureFromValues("arm", null, "/usr/lib/jvm/zulu17.38.21-ca-jdk17.0.5-linux_aarch32hf/lib", null), is(HardwareArchitecture.ARMHF));
        assertThat(PlatformInfo.detectHardwareArchitectureFromValues("arm", null, "/usr/lib/jvm/zulu17.38.21-ca-jdk17.0.5-linux_aarch32sf/lib", null), is(HardwareArchitecture.ARMEL));
        assertThat(PlatformInfo.detectHardwareArchitectureFromValues("armv7l", null, null, null), is(HardwareArchitecture.ARMHF));
    }

    @Test
    public void doDetectLinuxLibC() {
        // we can only perform this test on linux
        OperatingSystem operatingSystem = PlatformInfo.doDetectOperatingSystem();

        // this should actually not fail on any operating system
        LinuxLibC libc = PlatformInfo.detectLinuxLibC();

        if (operatingSystem == OperatingSystem.LINUX) {
            assertThat(libc, is(not(nullValue())));
            assertThat(libc, is(not(LinuxLibC.UNKNOWN)));
        } else {
            assertThat(libc, is(nullValue()));
        }
    }

}