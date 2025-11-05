import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.Task;
import com.fizzed.blaze.TaskGroup;
import com.fizzed.blaze.incubating.VcVars;
import com.fizzed.blaze.project.PublicBlaze;
import com.fizzed.buildx.Buildx;
import com.fizzed.buildx.Target;
import com.fizzed.jne.NativeTarget;
import com.fizzed.jne.OperatingSystem;

import java.nio.file.Path;
import java.util.*;

import static com.fizzed.blaze.Contexts.withBaseDir;
import static com.fizzed.blaze.Systems.*;
import static com.fizzed.blaze.Systems.exec;
import static com.fizzed.blaze.incubating.VisualStudios.vcVars;
import static com.fizzed.blaze.incubating.VisualStudios.vcVarsExec;
import static com.fizzed.blaze.util.Globber.globber;
import static java.util.Arrays.asList;

@TaskGroup(value="project", name="Project", order=1)
@TaskGroup(value="maintainers", name="Maintainers Only", order=2)
public class blaze extends PublicBlaze {

    private final NativeTarget localNativeTarget = NativeTarget.detect();
    private final Path nativeDir = projectDir.resolve("native");
    private final Path targetDir = projectDir.resolve("target");

    @Task(group="project", order = 0, value="Runs a demo of detecting all JDKs on this host")
    public void demo_detect_javas() throws Exception {
        // mvn process-test-classes exec:exec -Dexec.classpathScope=test -Dexec.executable=java -Dexec.args="-cp %classpath com.fizzed.jne.JavaHomesDemo"
        exec("mvn", "process-test-classes", "exec:exec",
            "-Dexec.classpathScope=test", "-Dexec.executable=java", "-Dexec.args=-cp %classpath com.fizzed.jne.JavaHomesDemo").run();
    }

    @Task(group="project", order = 1, value="Builds native libraries and executables for the local os/arch")
    public void build_natives() throws Exception {
        final String targetStr = Contexts.config().value("target").orNull();
        final NativeTarget nativeTarget = targetStr != null ? NativeTarget.fromJneTarget(targetStr) : NativeTarget.detect();

        log.info("Building natives for target {}", nativeTarget.toJneTarget());
        log.info("Copying native code to (cleaned) {} directory...", targetDir);
        rm(targetDir).recursive().force().run();
        mkdir(targetDir).parents().run();
        cp(globber(nativeDir, "*")).target(targetDir).recursive().debug().run();

        final Path targetJcatDir = targetDir.resolve("jcat");
        final Path targetLibHelloJDir = targetDir.resolve("libhelloj");
        final Path javaOutputDir = withBaseDir("../src/test/resources/jne/" + nativeTarget.toJneOsAbi() + "/" + nativeTarget.toJneArch());
        final String exename = nativeTarget.resolveExecutableFileName("jcat");
        final String libname = nativeTarget.resolveLibraryFileName("helloj");

        if (nativeTarget.getOperatingSystem() == OperatingSystem.WINDOWS) {
            // we may be cross-compiling so we pass the arch we want
            try (VcVars vcVars = vcVars().arch(nativeTarget.getHardwareArchitecture().name()).verbose().run()) {
                log.info("Building jcat executable...");
                vcVarsExec(vcVars, "nmake", "-f", "VCMakefile")
                    .workingDir(targetJcatDir)
                    .run();

                log.info("Building helloj library...");
                vcVarsExec(vcVars, "nmake", "-f", "VCMakefile")
                    .workingDir(targetLibHelloJDir)
                    .run();
            }
        } else {
            String cmd = "make";
            // freebsd and openbsd, we need to use gmake
            if (nativeTarget.getOperatingSystem() == OperatingSystem.FREEBSD || nativeTarget.getOperatingSystem() == OperatingSystem.OPENBSD) {
                cmd = "gmake";
            }

            log.info("Building jcat executable...");
            exec(cmd).workingDir(targetJcatDir).debug().run();

            log.info("Building helloj library...");
            exec(cmd).workingDir(targetLibHelloJDir).debug().run();
        }

        cp(targetJcatDir.resolve(exename)).target(javaOutputDir).force().verbose().run();
        cp(targetLibHelloJDir.resolve(libname)).target(javaOutputDir).force().verbose().run();
    }

    @Task(group="project", order = 3, value="Cleans up project target and cache dirs")
    public void nuke() throws Exception {
        rm(this.targetDir).recursive().force().verbose().run();
        rm(this.projectDir.resolve(".buildx")).recursive().force().verbose().run();
        rm(this.projectDir.resolve(".buildx-cache")).recursive().force().verbose().run();
        rm(this.projectDir.resolve(".buildx-logs")).recursive().force().verbose().run();
    }

    private final List<Target> crossBuildTargets = asList(
        // Linux
        new Target("linux", "x64").setTags("container").setContainerImage("docker.io/fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux-x64"),
        new Target("linux", "arm64").setTags("container").setContainerImage("docker.io/fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux-arm64"),
        new Target("linux", "riscv64").setTags("container").setContainerImage("docker.io/fizzed/buildx:x64-ubuntu18-jdk11-buildx-linux-riscv64"),
        new Target("linux", "armhf").setTags("container").setContainerImage("docker.io/fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux-armhf"),
        new Target("linux", "armel").setTags("container").setContainerImage("docker.io/fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux-armel"),
        // Linux (w/ MUSL)
        new Target("linux_musl", "x64").setTags("container").setContainerImage("docker.io/fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux_musl-x64"),
        new Target("linux_musl", "arm64").setTags("container").setContainerImage("docker.io/fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux_musl-arm64"),
        new Target("linux_musl", "riscv64").setTags("container").setContainerImage("docker.io/fizzed/buildx:x64-ubuntu18-jdk11-buildx-linux_musl-riscv64"),
        // FreeBSD
        new Target("freebsd", "x64").setTags("host").setHost("bmh-build-x64-freebsd-baseline"),
        new Target("freebsd", "arm64").setTags("host").setHost("bmh-build-arm64-freebsd-baseline"),
        // MacOS
        new Target("macos", "x64").setTags("host").setHost("bmh-build-x64-macos-baseline"),
        new Target("macos", "arm64").setTags("host").setHost("bmh-build-arm64-macos-baseline"),
        // OpenBSD
        new Target("openbsd", "x64").setTags("host").setHost("bmh-build-x64-openbsd-latest"),
        new Target("openbsd", "arm64").setTags("host").setHost("bmh-build-arm64-openbsd-latest"),
        // Windows
        new Target("windows", "x64").setTags("host").setHost("bmh-build-x64-windows-latest"),
        new Target("windows", "arm64").setTags("host").setHost("bmh-build-x64-windows-latest")
    );

    @Task(group="maintainers", order = 51, value="Builds native libraries and executables for various os/arch combos")
    public void cross_build_natives() throws Exception {
        final boolean serial = this.config.flag("serial").orElse(false);

        new Buildx(this.crossBuildTargets)
            .parallel(!serial)
            .execute((target, project) -> {
                // target name is like "linux-x64" which represents the os-arch we want to build a native for
                final String os = target.getName().split("-")[0];
                final String arch = target.getName().split("-")[1];

                project.exec("java", "-jar", "blaze.jar", "build_natives", "--target", target.getName())
                    .run();

                // we know that the only modified file will be in the artifact dir
                final String artifactRelPath = "src/test/resources/jne/" + os + "/" + arch + "/";
                project.rsync(artifactRelPath, artifactRelPath)
                    .run();
            });
    }

}