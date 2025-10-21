package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2023 Fizzed, Inc
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

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class NativeTargetTest {

    @Test
    public void getLibraryFileExtension() {
        NativeTarget nt;

        nt = NativeTarget.of(null, null, null);
        try {
            nt.getLibraryFileExtension();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        nt = NativeTarget.of(OperatingSystem.WINDOWS, null, null);
        assertThat(nt.getLibraryFileExtension(), is(".dll"));

        nt = NativeTarget.of(OperatingSystem.LINUX, null, null);
        assertThat(nt.getLibraryFileExtension(), is(".so"));

        nt = NativeTarget.of(OperatingSystem.FREEBSD, null, null);
        assertThat(nt.getLibraryFileExtension(), is(".so"));

        nt = NativeTarget.of(OperatingSystem.OPENBSD, null, null);
        assertThat(nt.getLibraryFileExtension(), is(".so"));

        nt = NativeTarget.of(OperatingSystem.SOLARIS, null, null);
        assertThat(nt.getLibraryFileExtension(), is(".so"));

        nt = NativeTarget.of(OperatingSystem.MACOS, null, null);
        assertThat(nt.getLibraryFileExtension(), is(".dylib"));
    }

    @Test
    public void resolveLibraryFileName() {
        NativeTarget nt;

        nt = NativeTarget.of(null, null, null);
        try {
            nt.resolveLibraryFileName("hello");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        nt = NativeTarget.of(OperatingSystem.WINDOWS, null, null);
        assertThat(nt.resolveLibraryFileName("hello"), is("hello.dll"));

        nt = NativeTarget.of(OperatingSystem.LINUX, null, null);
        assertThat(nt.resolveLibraryFileName("hello"), is("libhello.so"));

        nt = NativeTarget.of(OperatingSystem.FREEBSD, null, null);
        assertThat(nt.resolveLibraryFileName("hello"), is("libhello.so"));

        nt = NativeTarget.of(OperatingSystem.OPENBSD, null, null);
        assertThat(nt.resolveLibraryFileName("hello"), is("libhello.so"));

        nt = NativeTarget.of(OperatingSystem.SOLARIS, null, null);
        assertThat(nt.resolveLibraryFileName("hello"), is("libhello.so"));

        nt = NativeTarget.of(OperatingSystem.MACOS, null, null);
        assertThat(nt.resolveLibraryFileName("hello"), is("libhello.dylib"));
    }

    @Test
    public void getExecutableFileExtension() {
        NativeTarget nt;

        nt = NativeTarget.of(null, null, null);
        try {
            nt.getExecutableFileExtension();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        nt = NativeTarget.of(OperatingSystem.WINDOWS, null, null);
        assertThat(nt.getExecutableFileExtension(), is(".exe"));

        nt = NativeTarget.of(OperatingSystem.LINUX, null, null);
        assertThat(nt.getExecutableFileExtension(), is(nullValue()));

        nt = NativeTarget.of(OperatingSystem.FREEBSD, null, null);
        assertThat(nt.getExecutableFileExtension(), is(nullValue()));

        nt = NativeTarget.of(OperatingSystem.OPENBSD, null, null);
        assertThat(nt.getExecutableFileExtension(), is(nullValue()));

        nt = NativeTarget.of(OperatingSystem.SOLARIS, null, null);
        assertThat(nt.getExecutableFileExtension(), is(nullValue()));

        nt = NativeTarget.of(OperatingSystem.MACOS, null, null);
        assertThat(nt.getExecutableFileExtension(), is(nullValue()));
    }

    @Test
    public void resolveExecutableFileName() {
        NativeTarget nt;

        nt = NativeTarget.of(null, null, null);
        try {
            nt.resolveExecutableFileName("hello");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        nt = NativeTarget.of(OperatingSystem.WINDOWS, null, null);
        assertThat(nt.resolveExecutableFileName("hello"), is("hello.exe"));

        nt = NativeTarget.of(OperatingSystem.LINUX, null, null);
        assertThat(nt.resolveExecutableFileName("hello"), is("hello"));

        nt = NativeTarget.of(OperatingSystem.FREEBSD, null, null);
        assertThat(nt.resolveExecutableFileName("hello"), is("hello"));

        nt = NativeTarget.of(OperatingSystem.OPENBSD, null, null);
        assertThat(nt.resolveExecutableFileName("hello"), is("hello"));

        nt = NativeTarget.of(OperatingSystem.SOLARIS, null, null);
        assertThat(nt.resolveExecutableFileName("hello"), is("hello"));

        nt = NativeTarget.of(OperatingSystem.MACOS, null, null);
        assertThat(nt.resolveExecutableFileName("hello"), is("hello"));
    }

    @Test
    public void toRustTarget() {
        NativeTarget nt;

        nt = NativeTarget.of(null, null, null);
        try {
            nt.toRustTarget();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        nt = NativeTarget.of(OperatingSystem.WINDOWS, null, null);
        try {
            nt.toRustTarget();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.X64, null);
        assertThat(nt.toRustTarget(), is("x86_64-pc-windows-msvc"));

        nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.X64, ABI.MSVC);
        assertThat(nt.toRustTarget(), is("x86_64-pc-windows-msvc"));

        nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.X64, ABI.GNU);
        assertThat(nt.toRustTarget(), is("x86_64-pc-windows-gnu"));

        nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.X32, null);
        assertThat(nt.toRustTarget(), is("i686-pc-windows-msvc"));

        nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.ARM64, null);
        assertThat(nt.toRustTarget(), is("aarch64-pc-windows-msvc"));

        // NOTE: this doesn't actually exist, should we still generate a rust target value?
        nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.RISCV64, null);
        assertThat(nt.toRustTarget(), is("riscv64gc-pc-windows-msvc"));


        nt = NativeTarget.of(OperatingSystem.MACOS, HardwareArchitecture.X64, null);
        assertThat(nt.toRustTarget(), is("x86_64-apple-darwin"));

        nt = NativeTarget.of(OperatingSystem.MACOS, HardwareArchitecture.ARM64, null);
        assertThat(nt.toRustTarget(), is("aarch64-apple-darwin"));


        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X64, null);
        assertThat(nt.toRustTarget(), is("x86_64-unknown-linux-gnu"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X64, ABI.GNU);
        assertThat(nt.toRustTarget(), is("x86_64-unknown-linux-gnu"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X64, ABI.MUSL);
        assertThat(nt.toRustTarget(), is("x86_64-unknown-linux-musl"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X32, null);
        assertThat(nt.toRustTarget(), is("i686-unknown-linux-gnu"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X32, ABI.MUSL);
        assertThat(nt.toRustTarget(), is("i686-unknown-linux-musl"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.ARM64, null);
        assertThat(nt.toRustTarget(), is("aarch64-unknown-linux-gnu"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.ARM64, ABI.MUSL);
        assertThat(nt.toRustTarget(), is("aarch64-unknown-linux-musl"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.ARMHF, null);
        assertThat(nt.toRustTarget(), is("armv7-unknown-linux-gnueabihf"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.ARMEL, null);
        assertThat(nt.toRustTarget(), is("arm-unknown-linux-gnueabi"));



        nt = NativeTarget.of(OperatingSystem.FREEBSD, HardwareArchitecture.X64, null);
        assertThat(nt.toRustTarget(), is("x86_64-unknown-freebsd"));


        nt = NativeTarget.of(OperatingSystem.OPENBSD, HardwareArchitecture.X64, null);
        assertThat(nt.toRustTarget(), is("x86_64-unknown-openbsd"));


        nt = NativeTarget.of(OperatingSystem.SOLARIS, HardwareArchitecture.X64, null);
        assertThat(nt.toRustTarget(), is("x86_64-sun-solaris"));
    }

    @Test
    public void toAutoConfTarget() {
        NativeTarget nt;

        nt = NativeTarget.of(null, null, null);
        try {
            nt.toAutoConfTarget();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        nt = NativeTarget.of(OperatingSystem.WINDOWS, null, null);
        try {
            nt.toAutoConfTarget();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        /*nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.X64, null);
        assertThat(nt.toAutoConfTarget(), is("x86_64-pc-windows-msvc"));

        nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.X64, ABI.MSVC);
        assertThat(nt.toAutoConfTarget(), is("x86_64-pc-windows-msvc"));*/

        nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.X64, ABI.GNU);
        assertThat(nt.toAutoConfTarget(), is("x86_64-w64-mingw32"));

        nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.X32, ABI.GNU);
        assertThat(nt.toAutoConfTarget(), is("i686-w64-mingw32"));

        nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.ARM64, ABI.GNU);
        assertThat(nt.toAutoConfTarget(), is("aarch64-w64-mingw32"));


        nt = NativeTarget.of(OperatingSystem.MACOS, HardwareArchitecture.X64, null);
        assertThat(nt.toAutoConfTarget(), is("x86_64-apple-darwin"));

        nt = NativeTarget.of(OperatingSystem.MACOS, HardwareArchitecture.ARM64, null);
        assertThat(nt.toAutoConfTarget(), is("aarch64-apple-darwin"));


        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.RISCV64, null);
        assertThat(nt.toAutoConfTarget(), is("riscv64-linux-gnu"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X64, null);
        assertThat(nt.toAutoConfTarget(), is("x86_64-linux-gnu"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X64, ABI.GNU);
        assertThat(nt.toAutoConfTarget(), is("x86_64-linux-gnu"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X64, ABI.MUSL);
        assertThat(nt.toAutoConfTarget(), is("x86_64-linux-musl"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X32, null);
        assertThat(nt.toAutoConfTarget(), is("i686-linux-gnu"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X32, ABI.MUSL);
        assertThat(nt.toAutoConfTarget(), is("i686-linux-musl"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.ARM64, null);
        assertThat(nt.toAutoConfTarget(), is("aarch64-linux-gnu"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.ARM64, ABI.MUSL);
        assertThat(nt.toAutoConfTarget(), is("aarch64-linux-musl"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.ARMHF, null);
        assertThat(nt.toAutoConfTarget(), is("arm-linux-gnueabihf"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.ARMEL, null);
        assertThat(nt.toAutoConfTarget(), is("arm-linux-gnueabi"));

        /*nt = NativeTarget.of(OperatingSystem.FREEBSD, HardwareArchitecture.X64, null);
        assertThat(nt.toAutoConfTarget(), is("x86_64-unknown-freebsd"));


        nt = NativeTarget.of(OperatingSystem.OPENBSD, HardwareArchitecture.X64, null);
        assertThat(nt.toAutoConfTarget(), is("x86_64-unknown-openbsd"));


        nt = NativeTarget.of(OperatingSystem.SOLARIS, HardwareArchitecture.X64, null);
        assertThat(nt.toAutoConfTarget(), is("x86_64-sun-solaris"));*/
    }

    @Test
    public void toJneTarget() {
        NativeTarget nt;

        nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.X64, null);
        assertThat(nt.toJneOsAbi(), is("windows"));
        assertThat(nt.toJneArch(), is("x64"));
        assertThat(nt.toJneTarget(), is("windows-x64"));

        nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.X64, ABI.GNU);
        assertThat(nt.toJneOsAbi(), is("windows_gnu"));
        assertThat(nt.toJneArch(), is("x64"));
        assertThat(nt.toJneTarget(), is("windows_gnu-x64"));

        // invalid combo
        try {
            nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.X64, ABI.MUSL);
            nt.toJneOsAbi();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.ARM64, null);
        assertThat(nt.toJneOsAbi(), is("windows"));
        assertThat(nt.toJneArch(), is("arm64"));
        assertThat(nt.toJneTarget(), is("windows-arm64"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X64, null);
        assertThat(nt.toJneOsAbi(), is("linux"));
        assertThat(nt.toJneArch(), is("x64"));
        assertThat(nt.toJneTarget(), is("linux-x64"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X64, ABI.GNU);
        assertThat(nt.toJneOsAbi(), is("linux"));
        assertThat(nt.toJneArch(), is("x64"));
        assertThat(nt.toJneTarget(), is("linux-x64"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X64, ABI.MUSL);
        assertThat(nt.toJneOsAbi(), is("linux_musl"));
        assertThat(nt.toJneArch(), is("x64"));
        assertThat(nt.toJneTarget(), is("linux_musl-x64"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.ARMHF, ABI.GNU);
        assertThat(nt.toJneOsAbi(), is("linux"));
        assertThat(nt.toJneArch(), is("armhf"));
        assertThat(nt.toJneTarget(), is("linux-armhf"));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.ARMEL, ABI.GNU);
        assertThat(nt.toJneOsAbi(), is("linux"));
        assertThat(nt.toJneArch(), is("armel"));
        assertThat(nt.toJneTarget(), is("linux-armel"));
    }

    @Test
    public void fromJneTarget() {
        NativeTarget nt;

        try {
            NativeTarget.fromJneTarget(null);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            NativeTarget.fromJneTarget("blah");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            NativeTarget.fromJneTarget("-blayo");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            // nothing found
            NativeTarget.fromJneTarget("abb-blayo");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            // arch not found
            NativeTarget.fromJneTarget("windows-blayo");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            // abi not found
            NativeTarget.fromJneTarget("windows_blah-x64");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            // validate everything, but musl isn't supported on windows
            NativeTarget.fromJneTarget("windows_musl-x64");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        nt = NativeTarget.fromJneTarget("windows-x64");
        assertThat(nt.toJneOsAbi(), is("windows"));
        assertThat(nt.toJneArch(), is("x64"));
        assertThat(nt.toJneTarget(), is("windows-x64"));

        nt = NativeTarget.fromJneTarget("windows_msvc-x64");
        assertThat(nt.toJneOsAbi(), is("windows"));
        assertThat(nt.toJneArch(), is("x64"));
        assertThat(nt.toJneTarget(), is("windows-x64"));

        nt = NativeTarget.fromJneTarget("windows_gnu-x64");
        assertThat(nt.toJneOsAbi(), is("windows_gnu"));
        assertThat(nt.toJneArch(), is("x64"));
        assertThat(nt.toJneTarget(), is("windows_gnu-x64"));

        nt = NativeTarget.fromJneTarget("windows-arm64");
        assertThat(nt.toJneOsAbi(), is("windows"));
        assertThat(nt.toJneArch(), is("arm64"));
        assertThat(nt.toJneTarget(), is("windows-arm64"));

        nt = NativeTarget.fromJneTarget("linux-arm64");
        assertThat(nt.toJneOsAbi(), is("linux"));
        assertThat(nt.toJneArch(), is("arm64"));
        assertThat(nt.toJneTarget(), is("linux-arm64"));

        nt = NativeTarget.fromJneTarget("linux-armhf");
        assertThat(nt.toJneOsAbi(), is("linux"));
        assertThat(nt.toJneArch(), is("armhf"));
        assertThat(nt.toJneTarget(), is("linux-armhf"));

        nt = NativeTarget.fromJneTarget("linux-armel");
        assertThat(nt.toJneOsAbi(), is("linux"));
        assertThat(nt.toJneArch(), is("armel"));
        assertThat(nt.toJneTarget(), is("linux-armel"));

        nt = NativeTarget.fromJneTarget("linux_musl-x64");
        assertThat(nt.toJneOsAbi(), is("linux_musl"));
        assertThat(nt.toJneArch(), is("x64"));
        assertThat(nt.toJneTarget(), is("linux_musl-x64"));
    }

    @Test
    public void createResourcePaths() {
        NativeTarget nt;

        nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.X64, null);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(asList("/jne/windows/x64/test", "/jne/windows/x86_64/test", "/jne/windows/amd64/test")));

        nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.ARM64, null);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(asList("/jne/windows/arm64/test", "/jne/windows/aarch64/test")));

        nt = NativeTarget.of(OperatingSystem.WINDOWS, HardwareArchitecture.X32, null);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(asList("/jne/windows/x32/test", "/jne/windows/i386/test", "/jne/windows/i586/test", "/jne/windows/i686/test")));



        nt = NativeTarget.of(OperatingSystem.MACOS, HardwareArchitecture.X64, null);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(asList("/jne/macos/x64/test", "/jne/macos/x86_64/test", "/jne/macos/amd64/test", "/jne/osx/x64/test", "/jne/osx/x86_64/test", "/jne/osx/amd64/test", "/jne/darwin/x64/test", "/jne/darwin/x86_64/test", "/jne/darwin/amd64/test")));

        nt = NativeTarget.of(OperatingSystem.MACOS, HardwareArchitecture.ARM64, null);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(asList("/jne/macos/arm64/test", "/jne/macos/aarch64/test", "/jne/osx/arm64/test", "/jne/osx/aarch64/test", "/jne/darwin/arm64/test", "/jne/darwin/aarch64/test")));



        // optional arch
        nt = NativeTarget.of(OperatingSystem.WINDOWS, null, null);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(asList("/jne/windows/test")));

        // optional os
        nt = NativeTarget.of(null, HardwareArchitecture.X64, null);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(asList("/jne/x64/test", "/jne/x86_64/test", "/jne/amd64/test")));

        // optional os & arch
        nt = NativeTarget.of(null, null, null);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(Collections.singletonList("/jne/test")));



        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X64, null);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(asList("/jne/linux/x64/test", "/jne/linux/x86_64/test", "/jne/linux/amd64/test")));

        // ignore glibc
        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X64, ABI.GNU);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(asList("/jne/linux/x64/test", "/jne/linux/x86_64/test", "/jne/linux/amd64/test")));

        // musl (e.g. alpine)
        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X64, ABI.MUSL);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(asList("/jne/linux_musl/x64/test", "/jne/linux_musl/x86_64/test", "/jne/linux_musl/amd64/test")));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.X32, ABI.GNU);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(asList("/jne/linux/x32/test", "/jne/linux/i386/test", "/jne/linux/i586/test", "/jne/linux/i686/test")));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.RISCV64, ABI.GNU);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(asList("/jne/linux/riscv64/test")));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.RISCV64, ABI.MUSL);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(asList("/jne/linux_musl/riscv64/test")));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.ARM64, ABI.GNU);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(asList("/jne/linux/arm64/test", "/jne/linux/aarch64/test")));

        nt = NativeTarget.of(OperatingSystem.LINUX, HardwareArchitecture.ARM64, ABI.MUSL);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(asList("/jne/linux_musl/arm64/test", "/jne/linux_musl/aarch64/test")));
    }

    @Test
    public void detectFromText() {
        NativeTarget nativeTarget;

        nativeTarget = NativeTarget.detectFromText("https://download.freebsd.org/snapshots/ISO-IMAGES/15.0/FreeBSD-15.0-ALPHA5-arm64-aarch64-20251004-1c0898edf28f-280541-disc1.iso.xz");

        assertThat(nativeTarget.getOperatingSystem(), is(OperatingSystem.FREEBSD));
        assertThat(nativeTarget.getHardwareArchitecture(), is(HardwareArchitecture.ARM64));
        assertThat(nativeTarget.getAbi(), is(nullValue()));

        nativeTarget = NativeTarget.detectFromText("https://cdn.openbsd.org/pub/OpenBSD/7.7/amd64/install77.img");

        assertThat(nativeTarget.getOperatingSystem(), is(OperatingSystem.OPENBSD));
        assertThat(nativeTarget.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(nativeTarget.getAbi(), is(nullValue()));

        nativeTarget = NativeTarget.detectFromText("https://cdn.openbsd.org/pub/OpenBSD/7.7/riscv64/install77.img");

        assertThat(nativeTarget.getOperatingSystem(), is(OperatingSystem.OPENBSD));
        assertThat(nativeTarget.getHardwareArchitecture(), is(HardwareArchitecture.RISCV64));
        assertThat(nativeTarget.getAbi(), is(nullValue()));

        nativeTarget = NativeTarget.detectFromText("https://cloud-images.ubuntu.com/noble/current/noble-server-cloudimg-arm64.img");

        assertThat(nativeTarget.getOperatingSystem(), is(nullValue()));
        assertThat(nativeTarget.getHardwareArchitecture(), is(HardwareArchitecture.ARM64));
        assertThat(nativeTarget.getAbi(), is(nullValue()));

        // zulu java examples
        nativeTarget = NativeTarget.detectFromText("zulu7.56.0.11-ca-jre7.0.352-win_x64.msi");
        assertThat(nativeTarget.getOperatingSystem(), Matchers.is(OperatingSystem.WINDOWS));
        assertThat(nativeTarget.getHardwareArchitecture(), Matchers.is(HardwareArchitecture.X64));
        assertThat(nativeTarget.getAbi(), Matchers.is(Matchers.nullValue()));

        nativeTarget = NativeTarget.detectFromText("zulu17.54.21-ca-jre17.0.13-c2-linux_aarch32hf.tar.gz");
        assertThat(nativeTarget.getOperatingSystem(), Matchers.is(OperatingSystem.LINUX));
        assertThat(nativeTarget.getHardwareArchitecture(), Matchers.is(HardwareArchitecture.ARMHF));
        assertThat(nativeTarget.getAbi(), Matchers.is(Matchers.nullValue()));

        nativeTarget = NativeTarget.detectFromText("zulu11.76.21-ca-jdk11.0.25-linux_aarch32sf.tar.gz");
        assertThat(nativeTarget.getOperatingSystem(), Matchers.is(OperatingSystem.LINUX));
        assertThat(nativeTarget.getHardwareArchitecture(), Matchers.is(HardwareArchitecture.ARMEL));
        assertThat(nativeTarget.getAbi(), Matchers.is(Matchers.nullValue()));

        nativeTarget = NativeTarget.detectFromText("zulu11.76.21-ca-jre11.0.25-solaris_sparcv9.zip");
        assertThat(nativeTarget.getOperatingSystem(), Matchers.is(OperatingSystem.SOLARIS));
        //assertThat(nativeTarget.getHardwareArchitecture(), is(HardwareArchitecture.ARMEL));
        assertThat(nativeTarget.getAbi(), Matchers.is(Matchers.nullValue()));

        nativeTarget = NativeTarget.detectFromText("zulu11.76.21-ca-jre11.0.25-solaris_sparcv9.zip");
        assertThat(nativeTarget.getOperatingSystem(), Matchers.is(OperatingSystem.SOLARIS));
        //assertThat(nativeTarget.getHardwareArchitecture(), is(HardwareArchitecture.ARMEL));
        assertThat(nativeTarget.getAbi(), Matchers.is(Matchers.nullValue()));

        nativeTarget = NativeTarget.detectFromText("zulu8.82.0.23-ca-hl-jdk8.0.432-linux_ppc64.tar.gz");
        assertThat(nativeTarget.getOperatingSystem(), Matchers.is(OperatingSystem.LINUX));
        //assertThat(nativeTarget.getHardwareArchitecture(), is(HardwareArchitecture.ARMEL));
        assertThat(nativeTarget.getAbi(), Matchers.is(Matchers.nullValue()));

        // liberica java examples
        nativeTarget = NativeTarget.detectFromText("bellsoft-jre21.0.2+14-windows-i586.zip");
        assertThat(nativeTarget.getOperatingSystem(), Matchers.is(OperatingSystem.WINDOWS));
        assertThat(nativeTarget.getHardwareArchitecture(), Matchers.is(HardwareArchitecture.X32));
        assertThat(nativeTarget.getAbi(), Matchers.is(Matchers.nullValue()));

        nativeTarget = NativeTarget.detectFromText("bellsoft-jre21.0.2+14-macos-amd64.zip");
        assertThat(nativeTarget.getOperatingSystem(), Matchers.is(OperatingSystem.MACOS));
        assertThat(nativeTarget.getHardwareArchitecture(), Matchers.is(HardwareArchitecture.X64));
        assertThat(nativeTarget.getAbi(), Matchers.is(Matchers.nullValue()));

        nativeTarget = NativeTarget.detectFromText("bellsoft-jre21.0.2+14-macos-aarch64.zip");
        assertThat(nativeTarget.getOperatingSystem(), Matchers.is(OperatingSystem.MACOS));
        assertThat(nativeTarget.getHardwareArchitecture(), Matchers.is(HardwareArchitecture.ARM64));
        assertThat(nativeTarget.getAbi(), Matchers.is(Matchers.nullValue()));

        nativeTarget = NativeTarget.detectFromText("bellsoft-jre21.0.2+14-linux-x64-musl.apk");
        assertThat(nativeTarget.getOperatingSystem(), Matchers.is(OperatingSystem.LINUX));
        assertThat(nativeTarget.getHardwareArchitecture(), Matchers.is(HardwareArchitecture.X64));
        assertThat(nativeTarget.getAbi(), Matchers.is(ABI.MUSL));

        nativeTarget = NativeTarget.detectFromText("bellsoft-jre21.0.2+14-linux-riscv64.tar.gz");
        assertThat(nativeTarget.getOperatingSystem(), Matchers.is(OperatingSystem.LINUX));
        assertThat(nativeTarget.getHardwareArchitecture(), Matchers.is(HardwareArchitecture.RISCV64));
        assertThat(nativeTarget.getAbi(), Matchers.is(Matchers.nullValue()));

        nativeTarget = NativeTarget.detectFromText("bellsoft-jre21.0.2+14-linux-arm32-vfp-hflt.tar.gz");
        assertThat(nativeTarget.getOperatingSystem(), Matchers.is(OperatingSystem.LINUX));
        assertThat(nativeTarget.getHardwareArchitecture(), Matchers.is(HardwareArchitecture.ARMHF));
        assertThat(nativeTarget.getAbi(), Matchers.is(Matchers.nullValue()));

        nativeTarget = NativeTarget.detectFromText("bellsoft-jre21.0.2+14-linux-amd64.tar.gz");
        assertThat(nativeTarget.getOperatingSystem(), Matchers.is(OperatingSystem.LINUX));
        assertThat(nativeTarget.getHardwareArchitecture(), Matchers.is(HardwareArchitecture.X64));
        assertThat(nativeTarget.getAbi(), Matchers.is(Matchers.nullValue()));
    }

}