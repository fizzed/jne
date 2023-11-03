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

import com.fizzed.crux.util.Resources;
import com.fizzed.crux.util.StopWatch;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

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
        } catch (IOException e) {
            // expected since java isn't a real executable
        }
    }

    @Test
    public void fromDirectoryJdkZulu8() throws Exception {
        final Path javaHomeDir = this.mockJdksDir.resolve("jdk-zulu-8");

        final JavaHome javaHome = JavaHomes.fromDirectory(javaHomeDir);

        assertThat(javaHome.getJavaExe(), is(not(nullValue())));
        assertThat(javaHome.getNativeImageExe(), is(nullValue()));
        assertThat(javaHome.getVendor(), is("Azul Systems, Inc."));
        assertThat(javaHome.getDistribution(), is(JavaDistribution.ZULU));
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
        assertThat(javaHome.getAbi(), is(ABI.DEFAULT));
        assertThat(javaHome.getVersion().getMajor(), is(11));
        assertThat(javaHome.getVersion().getMinor(), is(0));
        assertThat(javaHome.getVersion().getSecurity(), is(17));
    }

    @Test
    public void fromDirectoryJdkZulu11Arm64Musl() throws Exception {
        final Path javaHomeDir = this.mockJdksDir.resolve("jdk-zulu-11-arm64-musl");

        final JavaHome javaHome = JavaHomes.fromDirectory(javaHomeDir);

        assertThat(javaHome.getJavaExe(), is(not(nullValue())));
        assertThat(javaHome.getVendor(), is("Azul Systems, Inc."));
        assertThat(javaHome.getHardwareArchitecture(), is(HardwareArchitecture.ARM64));
        assertThat(javaHome.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(javaHome.getAbi(), is(ABI.MUSL));
        assertThat(javaHome.getVersion().getMajor(), is(11));
        assertThat(javaHome.getVersion().getMinor(), is(0));
        assertThat(javaHome.getVersion().getSecurity(), is(20));
        assertThat(javaHome.getVersion().getBuild(), is(1));
    }

    @Test
    public void fromDirectoryJdkZulu11Armel() throws Exception {
        final Path javaHomeDir = this.mockJdksDir.resolve("jdk-zulu-11-armel");

        final JavaHome javaHome = JavaHomes.fromDirectory(javaHomeDir);

        assertThat(javaHome.getJavaExe(), is(not(nullValue())));
        assertThat(javaHome.getVendor(), is("Azul Systems, Inc."));
        assertThat(javaHome.getHardwareArchitecture(), is(HardwareArchitecture.ARMEL));
        assertThat(javaHome.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(javaHome.getAbi(), is(ABI.DEFAULT));
        assertThat(javaHome.getVersion().getMajor(), is(11));
        assertThat(javaHome.getVersion().getMinor(), is(0));
        assertThat(javaHome.getVersion().getSecurity(), is(20));
        assertThat(javaHome.getVersion().getBuild(), is(1));
    }

    @Test
    public void fromDirectoryJdkZulu11Armhf() throws Exception {
        final Path javaHomeDir = this.mockJdksDir.resolve("jdk-zulu-11-armhf");

        final JavaHome javaHome = JavaHomes.fromDirectory(javaHomeDir);

        assertThat(javaHome.getJavaExe(), is(not(nullValue())));
        assertThat(javaHome.getVendor(), is("Azul Systems, Inc."));
        assertThat(javaHome.getHardwareArchitecture(), is(HardwareArchitecture.ARMHF));
        assertThat(javaHome.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(javaHome.getAbi(), is(ABI.DEFAULT));
        assertThat(javaHome.getVersion().getMajor(), is(11));
        assertThat(javaHome.getVersion().getMinor(), is(0));
        assertThat(javaHome.getVersion().getSecurity(), is(19));
        assertThat(javaHome.getVersion().getBuild(), is(0));
    }

    @Test
    public void fromDirectoryJdk8Legacy() throws Exception {
        // resolve the jre directory of the jdk such as how java 8 does it
        final Path javaHomeJreDir = this.mockJdksDir.resolve("jdk-8-legacy/jre");

        final JavaHome javaHome = JavaHomes.fromDirectory(javaHomeJreDir);

        assertThat(javaHome.getJavaExe(), is(not(nullValue())));
        assertThat(javaHome.getVendor(), is("Azul Systems, Inc."));
        assertThat(javaHome.getHardwareArchitecture(), is(HardwareArchitecture.ARMHF));
        assertThat(javaHome.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(javaHome.getAbi(), is(ABI.DEFAULT));
        assertThat(javaHome.getVersion().getMajor(), is(11));
        assertThat(javaHome.getVersion().getMinor(), is(0));
        assertThat(javaHome.getVersion().getSecurity(), is(19));
        assertThat(javaHome.getVersion().getBuild(), is(0));
    }

    @Test
    public void fromDirectoryJdkLiberica21() throws Exception {
        final Path javaHomeJreDir = this.mockJdksDir.resolve("jdk-liberica-21");

        final JavaHome javaHome = JavaHomes.fromDirectory(javaHomeJreDir);

        assertThat(javaHome.getJavaExe(), is(not(nullValue())));
        assertThat(javaHome.getVendor(), is("BellSoft"));
        assertThat(javaHome.getDistribution(), is(JavaDistribution.LIBERICA));
        assertThat(javaHome.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(javaHome.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(javaHome.getAbi(), is(ABI.GNU));
        assertThat(javaHome.getVersion().getMajor(), is(21));
        assertThat(javaHome.getVersion().getMinor(), is(0));
        assertThat(javaHome.getVersion().getSecurity(), is(1));
        assertThat(javaHome.getVersion().getBuild(), is(0));
    }

    @Test
    public void fromDirectoryJreLiberica21() throws Exception {
        final Path javaHomeJreDir = this.mockJdksDir.resolve("jre-liberica-21");

        final JavaHome javaHome = JavaHomes.fromDirectory(javaHomeJreDir);

        assertThat(javaHome.getJavaExe(), is(not(nullValue())));
        assertThat(javaHome.getVendor(), is("BellSoft"));
        assertThat(javaHome.getDistribution(), is(JavaDistribution.LIBERICA));
        assertThat(javaHome.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(javaHome.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(javaHome.getAbi(), is(ABI.GNU));
        assertThat(javaHome.getVersion().getMajor(), is(21));
        assertThat(javaHome.getVersion().getMinor(), is(0));
        assertThat(javaHome.getVersion().getSecurity(), is(1));
        assertThat(javaHome.getVersion().getBuild(), is(0));
    }

    @Test
    public void fromDirectoryJdkCorretto17() throws Exception {
        final Path javaHomeJreDir = this.mockJdksDir.resolve("jdk-corretto-17");

        final JavaHome javaHome = JavaHomes.fromDirectory(javaHomeJreDir);

        assertThat(javaHome.getJavaExe(), is(not(nullValue())));
        assertThat(javaHome.getVendor(), is("Amazon.com Inc."));
        assertThat(javaHome.getDistribution(), is(JavaDistribution.CORRETTO));
        assertThat(javaHome.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(javaHome.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(javaHome.getAbi(), is(ABI.GNU));
        assertThat(javaHome.getVersion().getMajor(), is(17));
        assertThat(javaHome.getVersion().getMinor(), is(0));
        assertThat(javaHome.getVersion().getSecurity(), is(9));
        assertThat(javaHome.getVersion().getBuild(), is(0));
    }

    @Test
    public void fromDirectoryJdkMicrosoft17() throws Exception {
        final Path javaHomeJreDir = this.mockJdksDir.resolve("jdk-microsoft-17");

        final JavaHome javaHome = JavaHomes.fromDirectory(javaHomeJreDir);

        assertThat(javaHome.getJavaExe(), is(not(nullValue())));
        assertThat(javaHome.getVendor(), is("Microsoft"));
        assertThat(javaHome.getDistribution(), is(JavaDistribution.MICROSOFT));
        assertThat(javaHome.getImageType(), is(JavaImageType.JDK));
        assertThat(javaHome.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(javaHome.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(javaHome.getAbi(), is(ABI.GNU));
        assertThat(javaHome.getVersion().getMajor(), is(17));
        assertThat(javaHome.getVersion().getMinor(), is(0));
        assertThat(javaHome.getVersion().getSecurity(), is(9));
        assertThat(javaHome.getVersion().getBuild(), is(0));
    }

    @Test
    public void fromDirectoryJdkTemurin18() throws Exception {
        final Path javaHomeJreDir = this.mockJdksDir.resolve("jdk-temurin-18");

        final JavaHome javaHome = JavaHomes.fromDirectory(javaHomeJreDir);

        assertThat(javaHome.getJavaExe(), is(not(nullValue())));
        assertThat(javaHome.getVendor(), is("Eclipse Adoptium"));
        assertThat(javaHome.getDistribution(), is(JavaDistribution.TEMURIN));
        assertThat(javaHome.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(javaHome.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(javaHome.getAbi(), is(ABI.GNU));
        assertThat(javaHome.getVersion().getMajor(), is(18));
        assertThat(javaHome.getVersion().getMinor(), is(0));
        assertThat(javaHome.getVersion().getSecurity(), is(2));
        assertThat(javaHome.getVersion().getBuild(), is(1));
    }

    @Test
    public void fromDirectoryJdkSemeru17() throws Exception {
        final Path javaHomeJreDir = this.mockJdksDir.resolve("jdk-semeru-17");

        final JavaHome javaHome = JavaHomes.fromDirectory(javaHomeJreDir);

        assertThat(javaHome.getJavaExe(), is(not(nullValue())));
        assertThat(javaHome.getVendor(), is("IBM Corporation"));
        assertThat(javaHome.getDistribution(), is(JavaDistribution.SEMERU));
        assertThat(javaHome.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(javaHome.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(javaHome.getAbi(), is(ABI.GNU));
        assertThat(javaHome.getVersion().getMajor(), is(17));
        assertThat(javaHome.getVersion().getMinor(), is(0));
        assertThat(javaHome.getVersion().getSecurity(), is(8));
        assertThat(javaHome.getVersion().getBuild(), is(1));
    }

    @Test
    public void fromDirectoryJdkDragonwell11() throws Exception {
        final Path javaHomeJreDir = this.mockJdksDir.resolve("jdk-dragonwell-11");

        final JavaHome javaHome = JavaHomes.fromDirectory(javaHomeJreDir);

        assertThat(javaHome.getJavaExe(), is(not(nullValue())));
        assertThat(javaHome.getVendor(), is("Alibaba"));
        assertThat(javaHome.getDistribution(), is(JavaDistribution.DRAGONWELL));
        assertThat(javaHome.getHardwareArchitecture(), is(HardwareArchitecture.X64));
        assertThat(javaHome.getOperatingSystem(), is(OperatingSystem.LINUX));
        assertThat(javaHome.getAbi(), is(ABI.GNU));
        assertThat(javaHome.getVersion().getMajor(), is(11));
        assertThat(javaHome.getVersion().getMinor(), is(0));
        assertThat(javaHome.getVersion().getSecurity(), is(20));
        assertThat(javaHome.getVersion().getBuild(), is(17));
    }

    @Test
    public void readJdk6VersionOutput() throws Exception {
        final String output = Resources.stringUTF8("/jdkversions/oracle-jdk-6.txt");

        final Map<String,String> props = JavaHomes.readJavaVersionOutput(output);

        assertThat(props.get("JAVA_VERSION"), is("1.6.0_45"));
        assertThat(props.get("IMPLEMENTOR_VERSION"), is(nullValue()));
    }

    @Test
    public void readJdk7VersionOutput() throws Exception {
        // resolve the jre directory of the jdk such as how java 8 does it
        final String output = Resources.stringUTF8("/jdkversions/zulu-jdk-7.txt");

        final Map<String,String> props = JavaHomes.readJavaVersionOutput(output);

        assertThat(props.get("JAVA_VERSION"), is("1.7.0_352"));
        assertThat(props.get("IMPLEMENTOR_VERSION"), is("Zulu 7.56.0.11-CA-linux64"));
    }

    @Test
    public void readJdk11VersionOutput() throws Exception {
        // resolve the jre directory of the jdk such as how java 8 does it
        final String output = Resources.stringUTF8("/jdkversions/zulu-jdk-11.txt");

        final Map<String,String> props = JavaHomes.readJavaVersionOutput(output);

        assertThat(props.get("JAVA_VERSION"), is("11.0.17"));
        assertThat(props.get("IMPLEMENTOR_VERSION"), is("Zulu11.60+19-CA"));
    }

    @Test
    public void readJdk21VersionOutput() throws Exception {
        // resolve the jre directory of the jdk such as how java 8 does it
        final String output = Resources.stringUTF8("/jdkversions/graalvm-jdk-21.txt");

        final Map<String,String> props = JavaHomes.readJavaVersionOutput(output);

        assertThat(props.get("JAVA_VERSION"), is("21"));
        assertThat(props.get("IMPLEMENTOR_VERSION"), is("Oracle GraalVM 21+35.1"));
    }

    @Test
    public void executeJavaVersion() throws Exception {
        final JavaHome javaHome = JavaHome.current();

        final StopWatch timer = StopWatch.timeMillis();

        final String versionOutput = JavaHomes.executeJavaVersion(javaHome.getJavaExe());

        assertThat(versionOutput, is(not(nullValue())));
        /*System.out.println("Queried version in " + timer);
        System.out.println(versionOutput);*/
    }

    /*@Test
    public void test() throws Exception {
        System.getProperties().forEach((k,v) -> {
            System.out.println(k + " -> " + v);
        });
    }*/

}
