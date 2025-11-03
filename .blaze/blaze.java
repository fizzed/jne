import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.Task;
import com.fizzed.blaze.project.PublicBlaze;
import com.fizzed.buildx.Buildx;
import com.fizzed.buildx.ContainerBuilder;
import com.fizzed.buildx.Target;
import com.fizzed.jne.NativeTarget;
import com.fizzed.jne.OperatingSystem;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.fizzed.blaze.Contexts.withBaseDir;
import static com.fizzed.blaze.Systems.*;
import static com.fizzed.blaze.util.Globber.globber;
import static java.util.Arrays.asList;

public class blaze extends PublicBlaze {

    private final NativeTarget localNativeTarget = NativeTarget.detect();
    private final Path nativeDir = projectDir.resolve("native");
    private final Path targetDir = projectDir.resolve("target");

    @Task(order = 0)
    public void demo_detect_javas() throws Exception {
        // mvn process-test-classes exec:exec -Dexec.classpathScope=test -Dexec.executable=java -Dexec.args="-cp %classpath com.fizzed.jne.JavaHomesDemo"
        exec("mvn", "process-test-classes", "exec:exec",
            "-Dexec.classpathScope=test", "-Dexec.executable=java", "-Dexec.args=-cp %classpath com.fizzed.jne.JavaHomesDemo").run();
    }

    @Task(order = 1)
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
            // unfortunately its easiest to delegate this to helper script
            exec("setup/build-native-lib-windows-action.bat", nativeTarget.toJneOsAbi(), nativeTarget.toJneArch())
                .workingDir(this.projectDir)
                .verbose()
                .run();
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

    @Task(order = 2)
    public void test() throws Exception {
        exec("mvn", "test")
            .workingDir(this.projectDir)
            .verbose()
            .run();
    }

    @Task(order = 3)
    public void clean() throws Exception {
        rm(this.targetDir).recursive().force().verbose().run();
    }

    private final List<Target> crossBuildTargets = asList(
        // Linux
        new Target("linux", "x64").setTags("build").setContainerImage("docker.io/fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux-x64"),
        new Target("linux", "arm64").setTags("build").setContainerImage("docker.io/fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux-arm64"),
        new Target("linux", "riscv64").setTags("build").setContainerImage("docker.io/fizzed/buildx:x64-ubuntu18-jdk11-buildx-linux-riscv64"),
        new Target("linux", "armhf").setTags("build").setContainerImage("docker.io/fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux-armhf"),
        new Target("linux", "armel").setTags("build").setContainerImage("docker.io/fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux-armel"),
        // Linux (w/ MUSL)
        new Target("linux_musl", "x64").setTags("build").setContainerImage("docker.io/fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux_musl-x64"),
        new Target("linux_musl", "arm64").setTags("build").setContainerImage("docker.io/fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux_musl-arm64"),
        new Target("linux_musl", "riscv64").setTags("build").setContainerImage("docker.io/fizzed/buildx:x64-ubuntu18-jdk11-buildx-linux_musl-riscv64"),
        // FreeBSD
        new Target("freebsd", "x64").setTags("build").setHost("bmh-build-x64-freebsd-baseline"),
        new Target("freebsd", "arm64").setTags("build").setHost("bmh-build-arm64-freebsd-baseline"),
        // MacOS
        new Target("macos", "x64").setTags("build").setHost("bmh-build-x64-macos-baseline"),
        new Target("macos", "arm64").setTags("build").setHost("bmh-build-arm64-macos-baseline"),
        // OpenBSD
        new Target("openbsd", "x64").setTags("build").setHost("bmh-build-x64-openbsd-latest"),
        new Target("openbsd", "arm64").setTags("build").setHost("bmh-build-arm64-openbsd-latest"),
        // Windows
        new Target("windows", "x64").setTags("build").setHost("bmh-build-x64-windows-latest"),
        new Target("windows", "arm64").setTags("build").setHost("bmh-build-x64-windows-latest")
    );

    @Task(order = 50)
    public void cross_build_containers() throws Exception {
        new Buildx(crossBuildTargets)
            .containersOnly()
            .execute((target, project) -> {
                // no customization needed
                project.buildContainer(new ContainerBuilder()
                    //.setCache(false)
                );
            });
    }

    @Task(order = 51)
    public void cross_build_natives() throws Exception {
        new Buildx(crossBuildTargets)
            .tags("build")
            .parallel()
            .execute((target, project) -> {
                project.action("java", "-jar", "blaze.jar", "build_natives", "--target", target.getOsArch())
                    .run();

                // we know that the only modified file will be in the artifact dir
                final String artifactRelPath = "src/test/resources/jne/" + target.getOs() + "/" + target.getArch() + "/";
                project.rsync(artifactRelPath, artifactRelPath)
                    .run();
            });
    }

    @Override
    protected List<Target> crossTestTargets() {
        // everything but openbsd
        return super.crossTestTargets().stream()
            //.filter(v -> !v.getOs().contains("openbsd"))
            .collect(Collectors.toList());
    }

    public void cross_tests_parallel() throws Exception {
        new Buildx(this.crossTestTargets())
            .tags("test")
            .parallel()
            .execute((target, project) -> {
                project.action("echo", "hello world").run();
                /*project.action("mvn", "clean", "test")
                    .verbose()
                    .run();*/
            });
    }

}
