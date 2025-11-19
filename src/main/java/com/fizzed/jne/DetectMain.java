package com.fizzed.jne;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class DetectMain {

    static public void main(String[] args) throws Exception {
        // here are all the items we will attempt to detect
        List<JavaHome> javaHomes = null;
        PlatformInfo basicPlatformInfo = null;
        PlatformInfo allPlatformInfo = null;
        NativeTarget nativeTarget = null;
        UserEnvironment logicalUserEnvironment = null;
        UserEnvironment effectiveUserEnvironment = null;
        InstallEnvironment systemInstallEnvironment = null;
        InstallEnvironment userInstallEnvironment = null;

        // NOTE: very important we make this as bomb-proof as possible, so if the platform running it isn't supported
        // we will still display as much as we can

        logInfo("###################################################################################################");
        logInfo("Detecting Java Homes....");
        try {
            javaHomes = JavaHomes.detect();
        } catch (Throwable t) {
            logError("Unable to detect java homes", t);
        }
        logInfo("###################################################################################################");

        logInfo("###################################################################################################");
        logInfo("Detecting Basic Platform Info....");
        try {
            basicPlatformInfo = PlatformInfo.detectBasic();
        } catch (Throwable t) {
            logError("Unable to detect basic platform info", t);
        }
        logInfo("###################################################################################################");

        logInfo("###################################################################################################");
        logInfo("Detecting All Platform Info....");
        try {
            allPlatformInfo = PlatformInfo.detect(PlatformInfo.Detect.ALL);
        } catch (Throwable t) {
            logError("Unable to detect all platform info", t);
        }
        logInfo("###################################################################################################");

        logInfo("###################################################################################################");
        logInfo("Detecting Native Target....");
        try {
            nativeTarget = NativeTarget.detect();
        } catch (Throwable t) {
            logError("Unable to detect native target", t);
        }
        logInfo("###################################################################################################");

        logInfo("###################################################################################################");
        logInfo("Detecting Logical User Environment....");
        try {
            logicalUserEnvironment = UserEnvironment.detectLogical();
        } catch (Throwable t) {
            logError("Unable to detect logical user environment", t);
        }
        logInfo("###################################################################################################");

        logInfo("###################################################################################################");
        logInfo("Detecting Effective User Environment....");
        try {
            effectiveUserEnvironment = UserEnvironment.detectEffective();
        } catch (Throwable t) {
            logError("Unable to detect effective user environment", t);
        }
        logInfo("###################################################################################################");

        logInfo("###################################################################################################");
        logInfo("Detecting System Install Environment....");
        try {
            systemInstallEnvironment = InstallEnvironment.detect("JNE Demo", "jne-demo", EnvScope.SYSTEM);
        } catch (Throwable t) {
            logError("Unable to detect system install environment", t);
        }
        logInfo("###################################################################################################");

        logInfo("###################################################################################################");
        logInfo("Detecting User Install Environment....");
        try {
            userInstallEnvironment = InstallEnvironment.detect("JNE Demo", "jne-demo", EnvScope.USER);
        } catch (Throwable t) {
            logError("Unable to detect user install environment", t);
        }
        logInfo("###################################################################################################");



        logDetection("Java Homes", javaHomes, v -> {
            for (JavaHome javaHome : v) {
                logInfo("JavaHome: {}", javaHome.getDirectory());
                logInfo("  javaExe: {}", javaHome.getJavaExe());
                logInfo("  javacExe: {}", javaHome.getJavacExe());
                logInfo("  nativeImageExe: {}", javaHome.getNativeImageExe());
                logInfo("  imageType: {}", javaHome.getImageType());
                logInfo("  version: {}", javaHome.getVersion());
                logInfo("  os: {}", javaHome.getOperatingSystem());
                logInfo("  arch: {}", javaHome.getHardwareArchitecture());
                logInfo("  distro: {}", javaHome.getDistribution());
                logInfo("  vendor: {}", javaHome.getVendor());
            }
        });


        logDetection("Basic Platform Info", basicPlatformInfo, v -> {
            logInfo("operatingSystem: {}", v.getOperatingSystem());
            logInfo("hardwareArchitecture: {}", v.getHardwareArchitecture());
            logInfo("libC: {}", v.getLibC());
        });


        logDetection("All Platform Info", allPlatformInfo, v -> {
            logInfo("operatingSystem: {}", v.getOperatingSystem());
            logInfo("hardwareArchitecture: {}", v.getHardwareArchitecture());
            logInfo("name: {}", v.getName());
            logInfo("displayName: {}", v.getDisplayName());
            logInfo("version: {}", v.getVersion());
            logInfo("kernelVersion: {}", v.getKernelVersion());
            logInfo("uname: {}", v.getUname());
            logInfo("libC: {}", v.getLibC());
            logInfo("libCVersion: {}", v.getLibCVersion());
        });


        logDetection("Native Target", nativeTarget, v -> {
            logInfo("operatingSystem: {}", v.getOperatingSystem());
            logInfo("hardwareArchitecture: {}", v.getHardwareArchitecture());
            logInfo("abi: {}", v.getAbi());
            logInfo("executableFileExt: {}", v.getExecutableFileExtension());
            logInfo("executableFile (example): {}", v.resolveExecutableFileName("example"));
            logInfo("libraryFileExt: {}", v.getLibraryFileExtension());
            logInfo("libraryFile (example): {}", v.resolveLibraryFileName("example"));
            logInfo("resourcePathsForLibrary (example): {}", v.resolveResourcePaths("/jne", v.resolveLibraryFileName("example")));
            logInfo("jneTarget: {}", v.toJneTarget());
            logInfo("rustTarget: {}", v.toRustTarget());
            logInfo("autoConfTarget: {}", v.toAutoConfTarget());
        });


        logDetection("Logical User Environment", logicalUserEnvironment, v -> {
            logUserEnvironment(v);
        });


        logDetection("Effective User Environment", effectiveUserEnvironment, v -> {
            logUserEnvironment(v);
        });


        logDetection("System Install Environment", systemInstallEnvironment, v -> {
            logInstallEnvironment(v);
        });


        logDetection("User Install Environment", userInstallEnvironment, v -> {
            logInstallEnvironment(v);
        });
    }

    static public void logInfo(String format, Object... args) {
        // replace all {} placeholders in the string with the args
        String f = format.replace("{}", "%s");
        String msg = String.format(f, args);
        System.out.println("[INFO] " + msg);
    }

    static public void logError(String format, Object... args) {
        // replace all {} placeholders in the string with the args
        String f = format.replace("{}", "%s");
        String msg = String.format(f, args);
        System.out.println("[ERR ] " + msg);
    }

    static public <T> void logDetection(String title, T detection, Consumer<T> consumer) {
        Objects.requireNonNull(title);
        logInfo("###################################################################################################");
        logInfo("{} Detected:", title);
        logInfo("");
        if (detection != null) {
            // we also need to defend against exceptions while printing out info
            try {
                consumer.accept(detection);
            } catch (Throwable t) {
                logError("Unable to cleanly log detection: {}", t.getMessage(), t);
            }
        } else {
            logError("Unable to detect {}!", title.toLowerCase());
        }
        logInfo("");
        logInfo("###################################################################################################");
    }

    static public void logUserEnvironment(UserEnvironment userEnvironment) {
        logInfo("user: {}", userEnvironment.getUser());
        logInfo("elevated: {}", userEnvironment.isElevated());
        logInfo("displayName: {}", userEnvironment.getDisplayName());
        logInfo("userId: {}", userEnvironment.getUserId());
        logInfo("groupId: {}", userEnvironment.getGroupId());
        logInfo("homeDir: {}", userEnvironment.getHomeDir());
        logInfo("shell: {}", userEnvironment.getShell());
        logInfo("shellType: {}", userEnvironment.getShellType());
    }

    static public void logInstallEnvironment(InstallEnvironment installEnvironment) {
        logInfo("scope: {}", installEnvironment.getScope());
        logInfo("unitName: {}", installEnvironment.getUnitName());
        logInfo("applicationName: {}", installEnvironment.getApplicationName());
        logInfo("");
        logInfo("applicationRootDir: {}", installEnvironment.getApplicationRootDir());
        logInfo("systemRootDir: {}", installEnvironment.getSystemRootDir());
        logInfo("optRootDir: {}", installEnvironment.getOptRootDir());
        logInfo("localRootDir: {}", installEnvironment.getLocalRootDir());
        logInfo("");
        logInfo("systemBinDir: {}", installEnvironment.getSystemBinDir());
        logInfo("systemShareDir: {}", installEnvironment.getSystemShareDir());
        logInfo("");
        logInfo("applicationDir: {}", installEnvironment.getApplicationDir());
        logInfo("optApplicationDir: {}", installEnvironment.getOptApplicationDir());
        logInfo("localApplicationDir: {}", installEnvironment.getLocalApplicationDir());
        logInfo("localBinDir: {}", installEnvironment.getLocalBinDir());
        logInfo("localShareDir: {}", installEnvironment.getLocalShareDir());
    }

}