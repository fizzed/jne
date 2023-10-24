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



        nt = NativeTarget.of(OperatingSystem.FREEBSD, HardwareArchitecture.X64, null);
        assertThat(nt.toRustTarget(), is("x86_64-unknown-freebsd"));


        nt = NativeTarget.of(OperatingSystem.OPENBSD, HardwareArchitecture.X64, null);
        assertThat(nt.toRustTarget(), is("x86_64-unknown-openbsd"));


        nt = NativeTarget.of(OperatingSystem.SOLARIS, HardwareArchitecture.X64, null);
        assertThat(nt.toRustTarget(), is("x86_64-sun-solaris"));
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
            Matchers.is(asList("/jne/macos/x64/test", "/jne/macos/x86_64/test", "/jne/macos/amd64/test", "/jne/osx/x64/test", "/jne/osx/x86_64/test", "/jne/osx/amd64/test")));

        nt = NativeTarget.of(OperatingSystem.MACOS, HardwareArchitecture.ARM64, null);
        assertThat(nt.resolveResourcePaths("/jne", "test"),
            Matchers.is(asList("/jne/macos/arm64/test", "/jne/macos/aarch64/test", "/jne/osx/arm64/test", "/jne/osx/aarch64/test")));



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

}
