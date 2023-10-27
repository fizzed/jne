import com.fizzed.blaze.Context;
import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.Task;
import com.fizzed.blaze.system.Copy;
import com.fizzed.jne.NativeTarget;

import java.nio.file.Files;
import java.util.List;
import static java.util.Arrays.asList;
import java.nio.file.Path;

import static com.fizzed.blaze.Contexts.withBaseDir;
import static com.fizzed.blaze.Systems.exec;
import com.fizzed.buildx.*;

public class blaze {

    private final Path projectDir = withBaseDir("../").toAbsolutePath();
    private final NativeTarget localNativeTarget = NativeTarget.detect();

    @Task(order = 1)
    public void build_natives() throws Exception {
        final String targetStr = Contexts.config().value("target").orNull();
        final NativeTarget nativeTarget = targetStr != null ? NativeTarget.fromJneTarget(targetStr) : NativeTarget.detect();
        final Path nativeDir = projectDir.resolve("native");
        final Path targetDir = projectDir.resolve("target");
        final Path targetJcatDir = targetDir.resolve("jcat");
        final Path targetLibHelloJDir = targetDir.resolve("libhelloj");
        final Path javaOutputDir = withBaseDir("../src/test/resources/jne/" + nativeTarget.toJneOsAbi() + "/" + nativeTarget.toJneArch());
        final String libname = nativeTarget.resolveLibraryFileName("helloj");

        Files.createDirectories(targetDir);

        exec("rsync", "-avrt", "--delete", nativeDir+"/", targetDir+"/").run();

        exec("make").workingDir(targetJcatDir).run();

        exec("make").workingDir(targetLibHelloJDir)
            //.env("CXXFLAGS", "-z noexecstack")
            .run();

        new Copy(Contexts.currentContext())
            .source(targetJcatDir.resolve("jcat"))
            .destination(javaOutputDir)
            .force()
            .run();

        new Copy(Contexts.currentContext())
            .source(targetLibHelloJDir.resolve(libname))
            .destination(javaOutputDir)
            .force()
            .run();

        //Files.createDirectories(javaOutputDir);
        //Files.copy(targetJcatDir.resolve("jcat"), javaOutputDir.resolve("jcat"));

        /*cd ..
        OUTPUT_DIR="../src/test/resources/jne/${BUILDOS}/${BUILDARCH}"
        mkdir -p "$OUTPUT_DIR"
        cp jcat/jcat "$OUTPUT_DIR"
        cp libhelloj/libhelloj.so "$OUTPUT_DIR"*/
    }

    @Task(order = 2)
    public void test() throws Exception {
        /*exec("env")
            .workingDir(projectDir)
            .run();*/
        exec("mvn", "test")
            .workingDir(projectDir)
            .run();
    }

    private final List<Target> crossTargets = asList(
        //
        // Linux
        //

        new Target("linux", "x64", "ubuntu16.04, jdk11")
            .setTags("build")
            .setContainerImage("fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux-x64"),

        new Target("linux", "arm64", "ubuntu16.04, jdk11")
            .setTags("build")
            .setContainerImage("fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux-arm64"),

        new Target("linux", "armhf")
            .setTags("build")
            .setContainerImage("fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux-armhf"),

        new Target("linux", "armel")
            .setTags("build")
            .setContainerImage("fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux-armel"),

        // NOTE: ubuntu18 added support for riscv64
        new Target("linux", "riscv64")
            .setTags("build")
            .setContainerImage("fizzed/buildx:x64-ubuntu18-jdk11-buildx-linux-riscv64"),

        //
        // Linux (w/ MUSL)
        //

        new Target("linux_musl", "x64", "ubuntu16.04, jdk11")
            .setTags("build")
            .setContainerImage("fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux_musl-x64"),

        new Target("linux_musl", "arm64", "ubuntu16.04, jdk11")
            .setTags("build")
            .setContainerImage("fizzed/buildx:x64-ubuntu16-jdk11-buildx-linux_musl-arm64"),

        //
        // FreeBSD
        //

        new Target("freebsd", "x64")
            .setTags("build", "test")
            .setHost("bmh-build-x64-freebsd12-1"),

        new Target("freebsd", "arm64")
            .setTags("build", "test")
            .setHost("bmh-build-arm64-freebsd13-1"),

        //
        // OpenBSD
        //

        new Target("openbsd", "x64")
            .setTags("build", "test")
            .setHost("bmh-build-x64-openbsd67-1"),

        new Target("openbsd", "arm64")
            .setTags("build", "test")
            .setHost("bmh-build-arm64-openbsd72-1"),

        //
        // MacOS
        //

        new Target("macos", "x64")
            .setTags("build", "test")
            .setHost("bmh-build-x64-macos1013-1"),

        new Target("macos", "arm64")
            .setTags("build", "test")
            .setHost("bmh-build-arm64-macos12-1"),

        //
        // Windows
        //

        new Target("windows", "x64")
            .setTags("build", "test")
            .setHost("bmh-build-x64-win11-1"),

        new Target("windows", "arm64")
            .setTags("build")
            .setHost("bmh-build-x64-win11-1"),

        //
        // test-only containers
        //

        new Target(localNativeTarget.toJneOsAbi(), localNativeTarget.toJneArch(), "local machine")
            .setTags("test"),

        new Target("linux", "x64", "ubuntu16.04, jdk11")
            .setTags("test")
            .setContainerImage("fizzed/buildx:x64-ubuntu16-jdk11"),

        new Target("linux", "x64", "ubuntu22.04, jdk8")
            .setTags("test")
            .setContainerImage("fizzed/buildx:x64-ubuntu22-jdk8"),

        new Target("linux", "x64", "ubuntu22.04, jdk11")
            .setTags("test")
            .setContainerImage("fizzed/buildx:x64-ubuntu22-jdk11"),

        new Target("linux", "x64", "ubuntu22.04, jdk17")
            .setTags("test")
            .setContainerImage("fizzed/buildx:x64-ubuntu22-jdk17"),

        new Target("linux", "x64", "ubuntu22.04, jdk21")
            .setTags("test")
            .setContainerImage("fizzed/buildx:x64-ubuntu22-jdk21"),

        /*new Target("linux", "arm64")
            .setTags("test")
            .setHost("bmh-build-arm64-ubuntu22-1"),*/

        new Target("linux", "arm64", "ubuntu16.04, jdk11")
            .setTags("test")
            .setContainerImage("fizzed/buildx:arm64-ubuntu16-jdk11"),


        new Target("windows", "x64", "win10")
            .setTags("test")
            .setHost("bmh-build-x64-win10-1"),

        new Target("windows", "arm64", "win11")
            .setTags("test")
            .setHost("bmh-build-arm64-win11-1"),


        new Target("linux_musl", "x64", "alpine3.11, jdk11")
            .setTags("test")
            .setContainerImage("fizzed/buildx:x64-alpine3.11-jdk11"),

        new Target("linux_musl", "arm64", "alpine3.11, jdk11")
            .setTags("test")
            // faster to run on an arm64 box?
            //.setHost("bmh-build-arm64-ubuntu22-1")
            .setContainerImage("fizzed/buildx:arm64v8-alpine3.11-jdk11")

        /*
        */

        /*
        new Target("linux", "armhf-test")
            .setTags("test")
            .setContainerImage("fizzed/buildx:arm32v7-ubuntu18-jdk11"),

        new Target("linux", "armel-test")
            .setTags("test")
            .setContainerImage("fizzed/buildx:arm32v5-debian11-jdk11"),

        new Target("linux", "armel-test")
            .setTags("test")
            .setContainerImage("fizzed/buildx:arm32v5-debian11-jdk11"),

        new Target("linux", "riscv64-test")
            .setTags("test")
            .setContainerImage("fizzed/buildx:riscv64-ubuntu20-jdk19"),

        new Target("windows", "x64-test", "win10")
            .setTags("test")
            .setHost("bmh-build-x64-win10-1"),

        new Target("windows", "x64-test", "win7")
            .setTags("test")
            .setHost("bmh-build-x64-win7-1"),
        */
    );

    @Task(order = 50)
    public void cross_build_containers() throws Exception {
        new Buildx(crossTargets)
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
        new Buildx(crossTargets)
            .tags("build")
            .execute((target, project) -> {
                String buildScript = "setup/build-native-lib-linux-action.sh";
                if (target.getOs().equals("macos")) {
                    buildScript = "setup/build-native-lib-macos-action.sh";
                } else if (target.getOs().equals("windows")) {
                    buildScript = "setup/build-native-lib-windows-action.bat";
                }

                project.action(buildScript, target.getOs(), target.getArch()).run();

                // we know that the only modified file will be in the artifact dir
                final String artifactRelPath = "src/test/resources/jne/" + target.getOs() + "/" + target.getArch() + "/";
                project.rsync(artifactRelPath, artifactRelPath).run();
            });
    }

    @Task(order = 53)
    public void cross_tests() throws Exception {
        new Buildx(crossTargets)
            .tags("test")
            .execute((target, project) -> {
                project.action("java", "-jar", "blaze.jar", "test")
                    .run();
            });
    }

}
