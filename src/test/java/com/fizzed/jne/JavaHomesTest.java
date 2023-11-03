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

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.fail;

class JavaHomesTest {

    private Path mockJdksDir;

    public JavaHomesTest() throws Exception {
        this.mockJdksDir = new File(JavaHomesTest.class.getResource("/mockjdks/locate.txt").toURI()).toPath().resolve("..").normalize();
    }

    @Test
    public void fromDirectoryNotExists() throws Exception {
        final Path javaHomeDir = this.mockJdksDir.resolve("jdk-does-not-exist");

        try {
            JavaHomes.fromDirectory(javaHomeDir);
            fail();
        } catch (FileNotFoundException e) {
            assertThat(e.getMessage(), containsString("does not exist"));
        }
    }

    @Test
    public void fromDirectoryInvalidJdk() throws Exception {
        final Path javaHomeDir = this.mockJdksDir.resolve("jdk-invalid");

        try {
            JavaHomes.fromDirectory(javaHomeDir);
            fail();
        } catch (FileNotFoundException e) {
            // expected
        }
    }

    @Test
    public void fromDirectoryJdkMissingLibDir() throws Exception {
        final Path javaHomeDir = this.mockJdksDir.resolve("jdk-missing-lib");

        try {
            JavaHomes.fromDirectory(javaHomeDir);
            fail();
        } catch (FileNotFoundException e) {
            // expected
        }
    }

    @Test
    public void fromDirectoryJdkMissingReleaseFile() throws Exception {
        final Path javaHomeDir = this.mockJdksDir.resolve("jdk-missing-release");

        try {
            JavaHomes.fromDirectory(javaHomeDir);
            fail();
        } catch (FileNotFoundException e) {
            // expected
        }
    }

    @Test
    public void fromDirectoryJdkZulu8() throws Exception {
        final Path javaHomeDir = this.mockJdksDir.resolve("jdk-zulu-8");

        final JavaHome javaHome = JavaHomes.fromDirectory(javaHomeDir);

        assertThat(javaHome.getJavaExe(), is(not(nullValue())));
        assertThat(javaHome.getVendor(), is("Azul Systems, Inc."));
        assertThat(javaHome.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(javaHome.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(javaHome.getVersion().getMajor(), is(8));
        assertThat(javaHome.getVersion().getMinor(), is(0));
        assertThat(javaHome.getVersion().getSecurity(), is(352));
    }

    @Test
    public void fromDirectoryJdkZulu11() throws Exception {
        final Path javaHomeDir = this.mockJdksDir.resolve("jdk-zulu-11");

        final JavaHome javaHome = JavaHomes.fromDirectory(javaHomeDir);

        assertThat(javaHome.getJavaExe(), is(not(nullValue())));
        assertThat(javaHome.getVendor(), is("Azul Systems, Inc."));
        assertThat(javaHome.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(javaHome.getOperatingSystem(), is(OperatingSystem.WINDOWS));
        assertThat(javaHome.getVersion().getMajor(), is(11));
        assertThat(javaHome.getVersion().getMinor(), is(0));
        assertThat(javaHome.getVersion().getSecurity(), is(17));
    }

}
