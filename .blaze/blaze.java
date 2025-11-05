import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.Systems;
import com.fizzed.blaze.Task;
import com.fizzed.blaze.TaskGroup;
import com.fizzed.blaze.project.PublicBlaze;
import com.fizzed.blaze.system.Exec;
import com.fizzed.buildx.Buildx;
import com.fizzed.buildx.Target;
import com.fizzed.jne.NativeTarget;
import com.fizzed.jne.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.fizzed.blaze.Contexts.withBaseDir;
import static com.fizzed.blaze.Systems.*;
import static com.fizzed.blaze.Systems.exec;
import static com.fizzed.blaze.util.Globber.globber;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

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


    static public class VcVarSession implements AutoCloseable {

        private final Map<String,String> vcVars;
        private final List<Path> vcPaths;

        public VcVarSession(Map<String, String> vcVars, List<Path> vcPaths) {
            this.vcVars = vcVars;
            this.vcPaths = vcPaths;
        }

        public Map<String, String> getVcVars() {
            return vcVars;
        }

        public List<Path> getVcPaths() {
            return vcPaths;
        }

        @Override
        public void close() throws Exception {
            // do nothing
        }

    }

    static public class VisualStudios {
        static private final Logger log = LoggerFactory.getLogger(VisualStudios.class);

        static public Exec vcVarsExec(VcVarSession vcVarSession, String command, Object... arguments) {
            Exec exec = Systems.exec(command, arguments)
                .paths(vcVarSession.getVcPaths());

            vcVarSession.getVcVars().forEach(exec::env);

            return exec;
        }

        static public VcVarSession vcVarsSession(String targetArch, int... preferredYears) {
            final String vcVarsArch = resolveTargetArch(targetArch);

            // if no preferred years, use the default ones
            if (preferredYears == null || preferredYears.length == 0) {
                preferredYears = new int[] { 2022, 2019, 2017 };
            }

            // search for vcvarsall.bat file
            Path vcVarsAllBatFile = null;
            for (int year :  preferredYears) {
                Path f = Paths.get("C:\\Program Files\\Microsoft Visual Studio\\"+year+"\\Community\\VC\\Auxiliary\\Build\\vcvarsall.bat");
                log.debug("Searching vcvars @ {}", f);
                if (Files.exists(f)) {
                    vcVarsAllBatFile = f;
                    break;
                }
            }

            if (vcVarsAllBatFile == null) {
                throw new IllegalStateException("Could not find vcvarsall.bat for years " + Arrays.toString(preferredYears));
            }

            // get a snapshot of variables before running vcvarsall.bat
            final String preEnvOutput = exec("cmd", "/c", "set")
                .runCaptureOutput(false)
                .toString();

            // parse the before list into a map
            final Map<String,String> preEnv = parseEnvVars(preEnvOutput);

            // now call vcvarsall.bat, grab the adjusted env vars
            final String postEnvOutput = exec("cmd", "/c", "\"call \"" + vcVarsAllBatFile + "\" " + vcVarsArch + " & set\"")
                .runCaptureOutput(false)
                .toString();

            // parse the after list into a map
            final Map<String,String> postEnv = parseEnvVars(postEnvOutput);

            // calculate changes
            final Map<String,String> vcVars = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (Map.Entry<String,String> afterEntry : postEnv.entrySet()) {
                String envBeforeValue = preEnv.get(afterEntry.getKey());
                if (envBeforeValue == null) {
                    //log.info("NEW env: {} => {}",  afterEntry.getKey(), afterEntry.getValue());
                    vcVars.put(afterEntry.getKey(), afterEntry.getValue());
                } else if (!envBeforeValue.equals(afterEntry.getValue())) {
                    //log.info("CHANGED env: {} => {}",  afterEntry.getKey(), afterEntry.getValue());
                    vcVars.put(afterEntry.getKey(), envBeforeValue + "," + afterEntry.getValue());
                } else {
                    //log.info("SAME env: {} => {}",  afterEntry.getKey(), afterEntry.getValue());
                }
            }

            // for some reason the PATH is case sensitive when calling execs, and the PATH contains an odd "," in
            // some cases instead of ";", so we will remove the PATH, clean it up, and add it back
            final String uncleanPath = vcVars.remove("PATH");

            final List<Path> vcPaths = Arrays.stream(uncleanPath.split("[;,]"))
                .map(Paths::get)
                .collect(Collectors.toList());

            // build a better, improved path and set it as all caps
            final String sanitizedPath = vcPaths.stream()
                .map(Object::toString)
                .collect(joining(";"));

            vcVars.put("PATH", sanitizedPath);

            return new VcVarSession(vcVars, vcPaths);
        }

        static private Map<String,String> parseEnvVars(String output) {
            final Map<String,String> envVars = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

            for (String line : output.split("\n")) {
                String[] nv = line.trim().split("=");
                if (nv.length == 2) {
                    String name = nv[0].trim();
                    String value = nv[1].trim();
                    //log.info("{} => {}", name, value);
                    envVars.put(name, value);
                }
            }

            return envVars;
        }

        static private String resolveTargetArch(String targetArch) {
            // arch can be a few things, that we need to match
            final String hostArch = System.getProperty("os.arch");              // x86_64 or aarch64

            if ("x64".equalsIgnoreCase(targetArch)) {
                if ("x86_64".equalsIgnoreCase(hostArch) || "amd64".equalsIgnoreCase(hostArch)) {
                    return "x64";
                } else if ("aarch64".equalsIgnoreCase(hostArch)) {
                    return "x64_arm64";
                } else {
                    throw new IllegalArgumentException("Unknown mapping from host arch " + hostArch + " to target arch " + targetArch);
                }
            } else if ("arm64".equalsIgnoreCase(targetArch)) {
                if ("x86_64".equalsIgnoreCase(hostArch) || "amd64".equalsIgnoreCase(hostArch)) {
                    return "arm64_x64";
                } else if ("aarch64".equalsIgnoreCase(hostArch)) {
                    return "arm64";
                } else {
                    throw new IllegalArgumentException("Unknown mapping from host arch " + hostArch + " to target arch " + targetArch);
                }
            } else {
                throw new IllegalArgumentException("Unknown target arch " + targetArch + " (valid are x64 or arm64)");
            }
        }

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
            try (VcVarSession vcVarsSession = VisualStudios.vcVarsSession(nativeTarget.getHardwareArchitecture().name())) {
                log.info("Building jcat executable...");
                VisualStudios.vcVarsExec(vcVarsSession, "nmake", "-f", "VCMakefile")
                    .workingDir(targetJcatDir)
                    .run();

                log.info("Building helloj library...");
                VisualStudios.vcVarsExec(vcVarsSession, "nmake", "-f", "VCMakefile")
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