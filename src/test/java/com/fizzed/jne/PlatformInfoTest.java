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

import com.fizzed.crux.util.Resources;
import com.fizzed.jne.internal.SystemExecutorFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class PlatformInfoTest {
    static private final Logger log = LoggerFactory.getLogger(PlatformInfoTest.class);

    @Test
    public void detectAll() {
        PlatformInfo platformInfoBasic = PlatformInfo.detectBasic();
        PlatformInfo platformInfoAll = PlatformInfo.detectAll();

        // os & arch should be the same
        assertThat(platformInfoBasic.getOperatingSystem(), is(platformInfoAll.getOperatingSystem()));
        assertThat(platformInfoBasic.getHardwareArchitecture(), is(platformInfoAll.getHardwareArchitecture()));
        assertThat(platformInfoBasic.getLibC(), is(platformInfoAll.getLibC()));
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    public void detectAllLibCVersion() {
        PlatformInfo platformInfoAll = PlatformInfo.detectAll();

        // libc and libc version should not be null
        assertThat(platformInfoAll.getLibC(), is(not(nullValue())));
        assertThat(platformInfoAll.getLibCVersion(), is(not(nullValue())));
    }

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
        LibC libc = PlatformInfo.detectLinuxLibC();

        if (operatingSystem == OperatingSystem.LINUX) {
            assertThat(libc, is(not(nullValue())));
        } else {
            assertThat(libc, is(nullValue()));
        }
    }

    @Test
    public void ubuntu1604() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/ubuntu1604/exec-uname-a.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detectAll(fixtureExecutor);

        assertThat(platformInfo.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(platformInfo.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(platformInfo.getName(), is("Ubuntu"));
        assertThat(platformInfo.getDisplayName(), is("Ubuntu 16.04.7 LTS (Xenial Xerus)"));
        assertThat(platformInfo.getVersion(), is(SemanticVersion.parse("16.04")));
        assertThat(platformInfo.getKernelVersion(), is(SemanticVersion.parse("6.17.0.5")));
        assertThat(platformInfo.getUname(), is("Linux f599af415e3f 6.17.0-5-generic #5-Ubuntu SMP PREEMPT_DYNAMIC Mon Sep 22 10:00:33 UTC 2025 x86_64 x86_64 x86_64 GNU/Linux"));
        assertThat(platformInfo.getLibC(), is(LibC.GLIBC));
        assertThat(platformInfo.getLibCVersion(), is(SemanticVersion.parse("2.23")));
    }

    @Test
    public void ubuntu1804() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/ubuntu1804/exec-uname-a.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detectAll(fixtureExecutor);

        assertThat(platformInfo.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(platformInfo.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(platformInfo.getName(), is("Ubuntu"));
        assertThat(platformInfo.getDisplayName(), is("Ubuntu 18.04.6 LTS (Bionic Beaver)"));
        assertThat(platformInfo.getVersion(), is(SemanticVersion.parse("18.04")));
        assertThat(platformInfo.getKernelVersion(), is(SemanticVersion.parse("6.17.0.5")));
        assertThat(platformInfo.getUname(), is("Linux ec3ba564e1d9 6.17.0-5-generic #5-Ubuntu SMP PREEMPT_DYNAMIC Mon Sep 22 10:00:33 UTC 2025 x86_64 x86_64 x86_64 GNU/Linux"));
        assertThat(platformInfo.getLibC(), is(LibC.GLIBC));
        assertThat(platformInfo.getLibCVersion(), is(SemanticVersion.parse("2.27")));
    }

    @Test
    public void ubuntu2510() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/ubuntu2510/exec-uname-a.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detectAll(fixtureExecutor);

        assertThat(platformInfo.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(platformInfo.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(platformInfo.getName(), is("Ubuntu"));
        assertThat(platformInfo.getDisplayName(), is("Ubuntu 25.10 (Questing Quokka)"));
        assertThat(platformInfo.getVersion(), is(SemanticVersion.parse("25.10")));
        assertThat(platformInfo.getKernelVersion(), is(SemanticVersion.parse("6.17.0.6")));
        assertThat(platformInfo.getUname(), is("Linux bmh-jjlauer-4 6.17.0-6-generic #6-Ubuntu SMP PREEMPT_DYNAMIC Tue Oct  7 13:34:17 UTC 2025 x86_64 GNU/Linux"));
        assertThat(platformInfo.getLibC(), is(LibC.GLIBC));
        assertThat(platformInfo.getLibCVersion(), is(SemanticVersion.parse("2.42")));
    }


    @Test
    public void alpine315() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/alpine315/exec-uname-a.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detectAll(fixtureExecutor);

        assertThat(platformInfo.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(platformInfo.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(platformInfo.getName(), is("Alpine Linux"));
        assertThat(platformInfo.getDisplayName(), is("Alpine Linux v3.15"));
        assertThat(platformInfo.getVersion(), is(SemanticVersion.parse("3.15.11")));
        assertThat(platformInfo.getKernelVersion(), is(SemanticVersion.parse("5.15.140")));
        assertThat(platformInfo.getUname(), is("Linux bmh-build-x64-alpine315-1 5.15.140-0-virt #1-Alpine SMP Wed, 29 Nov 2023 21:47:33 +0000 x86_64 Linux"));
        assertThat(platformInfo.getLibC(), is(LibC.MUSL));
        assertThat(platformInfo.getLibCVersion(), is(SemanticVersion.parse("1.2.2")));
    }

}