package com.fizzed.jne.internal;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2025 Fizzed, Inc
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

import com.fizzed.crux.util.TemporaryPath;
import com.fizzed.jne.EnvPath;
import com.fizzed.jne.EnvVar;
import com.fizzed.jne.ShellType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ShellBuilderTest {
    static private final Logger log = LoggerFactory.getLogger(ShellBuilderTest.class);

    @Test
    public void bourneExportEnvVar() throws IOException, InterruptedException {
        // if /bin/sh is available, we will run this test
        final Path shellExe = Utils.which("sh");

        assumeTrue(shellExe != null, "No /bin/sh on local system, skipping test");

        try (TemporaryPath temporaryPath = TemporaryPath.tempFile("", ".sh")) {
            final ShellBuilder shellBuilder = new ShellBuilder(ShellType.SH);

            final List<String> shellLines = asList(
                shellBuilder.exportEnvVar(new EnvVar("TEST", "Hello World")),
                "echo \"$TEST\""
            );

            Utils.writeLinesToFile(temporaryPath.getPath(), shellLines, false);

            final String output = Utils.execAndGetOutput(asList(shellExe.toString(), temporaryPath.getPath().toString()));

            assertThat(output.trim(), is("Hello World"));
        }
    }

    @Test
    public void bourneAddEnvPath() throws IOException, InterruptedException {
        // if /bin/sh is available, we will run this test
        final Path shellExe = Utils.which("sh");

        assumeTrue(shellExe != null, "No /bin/sh on local system, skipping test");

        try (TemporaryPath temporaryPath = TemporaryPath.tempFile("", ".sh")) {
            final ShellBuilder shellBuilder = new ShellBuilder(ShellType.SH);

            final List<String> shellLines = asList(
                "PATH=/usr/bin:/usr/local/bin",
                // this should not be added again if our dup checks are working
                shellBuilder.addEnvPath(new EnvPath(Paths.get("/usr/bin"), false)),
                shellBuilder.addEnvPath(new EnvPath(Paths.get("/usr/bin"), true)),
                shellBuilder.addEnvPath(new EnvPath(Paths.get("/usr/local/bin"), false)),
                shellBuilder.addEnvPath(new EnvPath(Paths.get("/usr/local/bin"), true)),
                shellBuilder.addEnvPath(new EnvPath(Paths.get("/home/jjlauer/.local/bin"), true)),
                shellBuilder.addEnvPath(new EnvPath(Paths.get("/opt/bin"), false)),
                "echo \"$PATH\""
            );

            Utils.writeLinesToFile(temporaryPath.getPath(), shellLines, false);

            final String output = Utils.execAndGetOutput(asList(shellExe.toString(), temporaryPath.getPath().toString()));

            assertThat(output.trim(), is("/home/jjlauer/.local/bin:/usr/bin:/usr/local/bin:/opt/bin"));
        }
    }

    //
    // ZSH
    //

    @Test
    public void zshExportEnvVar() throws IOException, InterruptedException {
        // if /bin/sh is available, we will run this test
        final Path shellExe = Utils.which("zsh");

        assumeTrue(shellExe != null, "No /bin/zsh on local system, skipping test");

        try (TemporaryPath temporaryPath = TemporaryPath.tempFile("", ".sh")) {
            final ShellBuilder shellBuilder = new ShellBuilder(ShellType.SH);

            final List<String> shellLines = asList(
                shellBuilder.exportEnvVar(new EnvVar("TEST", "Hello World")),
                "echo \"$TEST\""
            );

            Utils.writeLinesToFile(temporaryPath.getPath(), shellLines, false);

            final String output = Utils.execAndGetOutput(asList(shellExe.toString(), temporaryPath.getPath().toString()));

            assertThat(output.trim(), is("Hello World"));
        }
    }

    @Test
    public void zshAddEnvPath() throws IOException, InterruptedException {
        // if /bin/sh is available, we will run this test
        final Path shellExe = Utils.which("zsh");

        assumeTrue(shellExe != null, "No /bin/zsh on local system, skipping test");

        try (TemporaryPath temporaryPath = TemporaryPath.tempFile("", ".sh")) {
            final ShellBuilder shellBuilder = new ShellBuilder(ShellType.SH);

            final List<String> shellLines = asList(
                "PATH=/usr/bin:/usr/local/bin",
                // this should not be added again if our dup checks are working
                shellBuilder.addEnvPath(new EnvPath(Paths.get("/usr/bin"), false)),
                shellBuilder.addEnvPath(new EnvPath(Paths.get("/usr/bin"), true)),
                shellBuilder.addEnvPath(new EnvPath(Paths.get("/usr/local/bin"), false)),
                shellBuilder.addEnvPath(new EnvPath(Paths.get("/usr/local/bin"), true)),
                shellBuilder.addEnvPath(new EnvPath(Paths.get("/home/jjlauer/.local/bin"), true)),
                shellBuilder.addEnvPath(new EnvPath(Paths.get("/opt/bin"), false)),
                "echo \"$PATH\""
            );

            Utils.writeLinesToFile(temporaryPath.getPath(), shellLines, false);

            final String output = Utils.execAndGetOutput(asList(shellExe.toString(), temporaryPath.getPath().toString()));

            assertThat(output.trim(), is("/home/jjlauer/.local/bin:/usr/bin:/usr/local/bin:/opt/bin"));
        }
    }

}
