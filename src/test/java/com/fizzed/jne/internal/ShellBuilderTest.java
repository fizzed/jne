package com.fizzed.jne.internal;

import com.fizzed.crux.util.TemporaryPath;
import com.fizzed.jne.EnvPath;
import com.fizzed.jne.EnvVar;
import com.fizzed.jne.ShellType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ShellBuilderTest {

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
    public void bourneExportEnvPath() throws IOException, InterruptedException {
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

}