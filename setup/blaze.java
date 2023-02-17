import org.slf4j.Logger;
import java.util.List;
import static java.util.Arrays.asList;
import java.nio.file.Path;
import java.util.function.Supplier;
import static java.util.stream.Collectors.toList;
import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.system.Exec;
import static com.fizzed.blaze.Contexts.withBaseDir;
import static com.fizzed.blaze.Contexts.fail;
import com.fizzed.blaze.Systems;
import static com.fizzed.blaze.Systems.exec;
import com.fizzed.blaze.ssh.SshSession;
import static com.fizzed.blaze.SecureShells.sshConnect;
import static com.fizzed.blaze.SecureShells.sshExec;
import com.fizzed.buildx.*;

public class blaze {

    private final List<Target> targets = asList(
        //
        // Linux
        //

        new Target("linux", "x64")
            .setTags("build")
            .setContainerImage("fizzed/buildx:amd64-ubuntu16-jdk11-cross-build"),

        new Target("linux", "arm64")
            .setTags("build")
            .setContainerImage("fizzed/buildx:amd64-ubuntu16-jdk11-cross-build"),

        new Target("linux", "armhf")
            .setTags("build")
            .setContainerImage("fizzed/buildx:amd64-ubuntu16-jdk11-cross-build"),

        new Target("linux", "armel")
            .setTags("build")
            .setContainerImage("fizzed/buildx:amd64-ubuntu16-jdk11-cross-build"),

        // NOTE: ubuntu18 added support for riscv64
        new Target("linux", "riscv64")
            .setTags("build")
            .setContainerImage("fizzed/buildx:amd64-ubuntu18-jdk11-cross-build"),

        //
        // Linux (w/ MUSL)
        //

        new Target("linux_musl", "x64")
            .setTags("build")
            .setContainerImage("fizzed/buildx:amd64-ubuntu16-jdk11-cross-build"),

        new Target("linux_musl", "arm64")
            .setTags("build")
            .setContainerImage("fizzed/buildx:amd64-ubuntu16-jdk11-cross-build"),

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

        new Target("linux", "x64-test")
            .setTags("test")
            .setContainerImage("fizzed/buildx:amd64-ubuntu16-jdk11"),

        new Target("linux", "arm64-test")
            .setTags("test")
            .setHost("bmh-build-arm64-ubuntu22-1"),

        new Target("linux_musl", "x64-test")
            .setTags("test")
            .setContainerImage("fizzed/buildx:amd64-alpine3.11-jdk11"),

        new Target("windows", "arm64-test", "win11")
            .setTags("test")
            .setHost("bmh-build-arm64-win11-1")

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

        new Target("linux_musl", "arm64-test")
            .setTags("test")
            //.setHost("bmh-build-arm64-ubuntu22-1")
            .setContainerImage("fizzed/buildx:arm64v8-alpine3.11-jdk11"),

        new Target("windows", "x64-test", "win10")
            .setTags("test")
            .setHost("bmh-build-x64-win10-1"),

        new Target("windows", "x64-test", "win7")
            .setTags("test")
            .setHost("bmh-build-x64-win7-1"),
        */
    );

    public void build_containers() throws Exception {
        new Buildx(targets)
            .execute((target, project) -> {
                if (project.hasContainer()) {
                    project.exec("setup/build-container-action.sh", target.getContainerImage(), project.getContainerName(), target.getOs(), target.getArch()).run();
                }
            });
    }

    public void build_native_libs() throws Exception {
        new Buildx(targets)
            .setTags("build")
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

    public void tests() throws Exception {
        new Buildx(targets)
            .setTags("test")
            .execute((target, project) -> {
                String testScript = "setup/test-project-action.sh";
                if (target.getOs().equals("windows")) {
                    testScript = "setup/test-project-action.bat";
                }

                project.action(testScript).run();
            });
    }

}
