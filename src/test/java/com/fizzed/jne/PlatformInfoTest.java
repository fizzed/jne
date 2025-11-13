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
        PlatformInfo platformInfoAll = PlatformInfo.detect(PlatformInfo.Detect.ALL);

        // os & arch should be the same
        assertThat(platformInfoBasic.getOperatingSystem(), is(platformInfoAll.getOperatingSystem()));
        assertThat(platformInfoBasic.getHardwareArchitecture(), is(platformInfoAll.getHardwareArchitecture()));
        assertThat(platformInfoBasic.getLibC(), is(platformInfoAll.getLibC()));
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    public void detectAllLibCVersion() {
        PlatformInfo platformInfoAll = PlatformInfo.detect(PlatformInfo.Detect.ALL);

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

    //
    // Real World Tests
    //

    @Test
    public void ubuntu1604() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/ubuntu1604/exec-uname-a.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detect(fixtureExecutor, PlatformInfo.Detect.ALL);

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

        final PlatformInfo platformInfo = PlatformInfo.detect(fixtureExecutor, PlatformInfo.Detect.ALL);

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

        final PlatformInfo platformInfo = PlatformInfo.detect(fixtureExecutor, PlatformInfo.Detect.ALL);

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
    public void ubuntu2404_arm64() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/ubuntu2404-arm64/exec-uname-a.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detect(fixtureExecutor, PlatformInfo.Detect.ALL);

        assertThat(platformInfo.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(platformInfo.getHardwareArchitecture(), is(HardwareArchitecture.ARM64));
        assertThat(platformInfo.getName(), is("Ubuntu"));
        assertThat(platformInfo.getDisplayName(), is("Ubuntu 24.04.3 LTS (Noble Numbat)"));
        assertThat(platformInfo.getVersion(), is(SemanticVersion.parse("24.04")));
        assertThat(platformInfo.getKernelVersion(), is(SemanticVersion.parse("6.8.0.87")));
        assertThat(platformInfo.getUname(), is("Linux bmh-build-arm64-ubuntu24-1 6.8.0-87-generic #88-Ubuntu SMP PREEMPT_DYNAMIC Sat Oct 11 09:16:38 UTC 2025 aarch64 aarch64 aarch64 GNU/Linux"));
        assertThat(platformInfo.getLibC(), is(LibC.GLIBC));
        assertThat(platformInfo.getLibCVersion(), is(SemanticVersion.parse("2.39")));
    }

    @Test
    public void ubuntu2404_riscv64() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/ubuntu2404-riscv64/exec-uname-a.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detect(fixtureExecutor, PlatformInfo.Detect.ALL);

        assertThat(platformInfo.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(platformInfo.getHardwareArchitecture(), is(HardwareArchitecture.RISCV64));
        assertThat(platformInfo.getName(), is("Ubuntu"));
        assertThat(platformInfo.getDisplayName(), is("Ubuntu 24.04.3 LTS (Noble Numbat)"));
        assertThat(platformInfo.getVersion(), is(SemanticVersion.parse("24.04")));
        assertThat(platformInfo.getKernelVersion(), is(SemanticVersion.parse("6.14.0.35")));
        assertThat(platformInfo.getUname(), is("Linux bmh-build-riscv64-ubuntu24-1 6.14.0-35-generic #35.1~24.04.1-Ubuntu SMP PREEMPT_DYNAMIC Thu Oct 16 08:15:18 UTC riscv64 riscv64 riscv64 GNU/Linux"));
        assertThat(platformInfo.getLibC(), is(LibC.GLIBC));
        assertThat(platformInfo.getLibCVersion(), is(SemanticVersion.parse("2.39")));
    }

    @Test
    public void fedora42_arm64() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/fedora42-arm64/exec-uname-a.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detect(fixtureExecutor, PlatformInfo.Detect.ALL);

        assertThat(platformInfo.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(platformInfo.getHardwareArchitecture(), is(HardwareArchitecture.ARM64));
        assertThat(platformInfo.getName(), is("Fedora Linux Asahi Remix"));
        assertThat(platformInfo.getDisplayName(), is("Fedora Linux Asahi Remix 42 (Forty Two [Adams])"));
        assertThat(platformInfo.getVersion(), is(SemanticVersion.parse("42")));
        assertThat(platformInfo.getKernelVersion(), is(SemanticVersion.parse("6.16.8.400")));
        assertThat(platformInfo.getUname(), is("Linux bmh-mini-2 6.16.8-400.asahi.fc42.aarch64+16k #1 SMP PREEMPT_DYNAMIC Sun Sep 21 20:31:36 UTC 2025 aarch64 GNU/Linux"));
        assertThat(platformInfo.getLibC(), is(LibC.GLIBC));
        assertThat(platformInfo.getLibCVersion(), is(SemanticVersion.parse("2.41")));
    }

    @Test
    public void alpine315() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/alpine315/exec-uname-a.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detect(fixtureExecutor, PlatformInfo.Detect.ALL);

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

    @Test
    public void macos11() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/macos11/exec-uname-a.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detect(fixtureExecutor, PlatformInfo.Detect.ALL);

        assertThat(platformInfo.getOperatingSystem(), is(OperatingSystem.MACOS));
        assertThat(platformInfo.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(platformInfo.getName(), is("macOS"));
        assertThat(platformInfo.getDisplayName(), is("macOS 11.7.2 (Big Sur)"));
        assertThat(platformInfo.getVersion(), is(SemanticVersion.parse("11.7.2")));
        assertThat(platformInfo.getKernelVersion(), is(SemanticVersion.parse("20.6")));
        assertThat(platformInfo.getUname(), is("Darwin bmh-build-x64-macos11-1.lauer.lan 20.6.0 Darwin Kernel Version 20.6.0: Sun Nov  6 23:17:00 PST 2022; root:xnu-7195.141.46~1/RELEASE_X86_64 x86_64"));
        assertThat(platformInfo.getLibC(), is(nullValue()));
        assertThat(platformInfo.getLibCVersion(), is(nullValue()));
    }

    @Test
    public void macos15() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/macos15/exec-uname-a.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detect(fixtureExecutor, PlatformInfo.Detect.ALL);

        assertThat(platformInfo.getOperatingSystem(), is(OperatingSystem.MACOS));
        assertThat(platformInfo.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(platformInfo.getName(), is("macOS"));
        assertThat(platformInfo.getDisplayName(), is("macOS 15.1 (Sequoia)"));
        assertThat(platformInfo.getVersion(), is(SemanticVersion.parse("15.1")));
        assertThat(platformInfo.getKernelVersion(), is(SemanticVersion.parse("24.1")));
        assertThat(platformInfo.getUname(), is("Darwin bmh-build-x64-macos15-1.lauer.lan 24.1.0 Darwin Kernel Version 24.1.0: Thu Oct 10 21:02:27 PDT 2024; root:xnu-11215.41.3~2/RELEASE_X86_64 x86_64"));
        assertThat(platformInfo.getLibC(), is(nullValue()));
        assertThat(platformInfo.getLibCVersion(), is(nullValue()));
    }

    @Test
    public void macos15arm64() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/macos15-arm64/exec-uname-a.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detect(fixtureExecutor, PlatformInfo.Detect.ALL);

        assertThat(platformInfo.getOperatingSystem(), is(OperatingSystem.MACOS));
        assertThat(platformInfo.getHardwareArchitecture(), is(HardwareArchitecture.ARM64));
        assertThat(platformInfo.getName(), is("macOS"));
        assertThat(platformInfo.getDisplayName(), is("macOS 15.7.1 (Sequoia)"));
        assertThat(platformInfo.getVersion(), is(SemanticVersion.parse("15.7.1")));
        assertThat(platformInfo.getKernelVersion(), is(SemanticVersion.parse("24.6")));
        assertThat(platformInfo.getUname(), is("Darwin bmh-dev-arm64-macos15-1.lauer.lan 24.6.0 Darwin Kernel Version 24.6.0: Mon Aug 11 21:16:05 PDT 2025; root:xnu-11417.140.69.701.11~1/RELEASE_ARM64_VMAPPLE arm64"));
        assertThat(platformInfo.getLibC(), is(nullValue()));
        assertThat(platformInfo.getLibCVersion(), is(nullValue()));
    }

    @Test
    public void freebsd13() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/freebsd13/exec-uname-a.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detect(fixtureExecutor, PlatformInfo.Detect.ALL);

        assertThat(platformInfo.getOperatingSystem(), is(OperatingSystem.FREEBSD));
        assertThat(platformInfo.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(platformInfo.getName(), is("FreeBSD"));
        assertThat(platformInfo.getDisplayName(), is("FreeBSD 13.5-RELEASE-p6"));
        assertThat(platformInfo.getVersion(), is(SemanticVersion.parse("13.5")));
        assertThat(platformInfo.getKernelVersion(), is(nullValue()));
        assertThat(platformInfo.getUname(), is("FreeBSD bmh-build-x64-freebsd13-1 13.5-RELEASE-p6 FreeBSD 13.5-RELEASE-p6 GENERIC amd64"));
        assertThat(platformInfo.getLibC(), is(nullValue()));
        assertThat(platformInfo.getLibCVersion(), is(nullValue()));
    }

    @Test
    public void openbsd78() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/openbsd78/exec-uname-a.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detect(fixtureExecutor, PlatformInfo.Detect.ALL);

        assertThat(platformInfo.getOperatingSystem(), is(OperatingSystem.OPENBSD));
        assertThat(platformInfo.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(platformInfo.getName(), is("OpenBSD"));
        assertThat(platformInfo.getDisplayName(), is("OpenBSD 7.8"));
        assertThat(platformInfo.getVersion(), is(SemanticVersion.parse("7.8")));
        assertThat(platformInfo.getKernelVersion(), is(nullValue()));
        assertThat(platformInfo.getUname(), is("OpenBSD bmh-build-x64-openbsd78-1 7.8 GENERIC.MP#54 amd64"));
        assertThat(platformInfo.getLibC(), is(nullValue()));
        assertThat(platformInfo.getLibCVersion(), is(nullValue()));
    }

    @Test
    public void windows11() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/windows11/locate.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detect(fixtureExecutor, PlatformInfo.Detect.ALL);

        assertThat(platformInfo.getOperatingSystem(), is(OperatingSystem.WINDOWS));
        assertThat(platformInfo.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(platformInfo.getName(), is("Windows"));
        assertThat(platformInfo.getDisplayName(), is("Windows 11 Pro (25H2)"));
        assertThat(platformInfo.getVersion(), is(SemanticVersion.parse("11.0.26200")));
        assertThat(platformInfo.getKernelVersion(), is(SemanticVersion.parse("10.0.26200")));
        assertThat(platformInfo.getUname(), is("Windows BMH-BUILD-X64-W 11.0.26200 Windows 11 Pro (25H2) AMD64"));
        assertThat(platformInfo.getLibC(), is(nullValue()));
        assertThat(platformInfo.getLibCVersion(), is(nullValue()));
    }

    @Test
    public void windows7() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/windows7/locate.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detect(fixtureExecutor, PlatformInfo.Detect.ALL);

        assertThat(platformInfo.getOperatingSystem(), is(OperatingSystem.WINDOWS));
        assertThat(platformInfo.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(platformInfo.getName(), is("Windows"));
        assertThat(platformInfo.getDisplayName(), is("Windows 7 Professional"));
        assertThat(platformInfo.getVersion(), is(SemanticVersion.parse("7.0.7601")));
        assertThat(platformInfo.getKernelVersion(), is(SemanticVersion.parse("6.1.7601")));
        assertThat(platformInfo.getUname(), is("Windows BMH-BUILD-X64-W 7.0.7601 Windows 7 Professional AMD64"));
        assertThat(platformInfo.getLibC(), is(nullValue()));
        assertThat(platformInfo.getLibCVersion(), is(nullValue()));
    }

    @Test
    public void voidLinux() throws Exception {
        final Path dir = Resources.file("/fixtures/platforms/voidlinux/exec-uname-a.txt").getParent();
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(dir);

        final PlatformInfo platformInfo = PlatformInfo.detect(fixtureExecutor, PlatformInfo.Detect.ALL);

        assertThat(platformInfo.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(platformInfo.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(platformInfo.getName(), is("Void"));
        assertThat(platformInfo.getDisplayName(), is("Void Linux"));
        assertThat(platformInfo.getVersion(), is(nullValue()));
        assertThat(platformInfo.getKernelVersion(), is(SemanticVersion.parse("6.17.0.6")));
        assertThat(platformInfo.getUname(), is("Linux 3febf31f2dfb 6.17.0-6-generic #6-Ubuntu SMP PREEMPT_DYNAMIC Tue Oct  7 13:34:17 UTC 2025 x86_64 GNU/Linux"));
        assertThat(platformInfo.getLibC(), is(LibC.MUSL));
        assertThat(platformInfo.getLibCVersion(), is(SemanticVersion.parse("1.2.5")));
    }

}