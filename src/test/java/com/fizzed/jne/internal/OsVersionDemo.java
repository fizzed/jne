package com.fizzed.jne.internal;

import com.fizzed.jne.NativeTarget;
import com.fizzed.jne.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsVersionDemo {
    static private final Logger log = LoggerFactory.getLogger(OsVersionDemo.class);

    static public void main(String[] args) throws Exception {
        final NativeTarget nativeTarget = NativeTarget.detect();
        final SystemExecutor systemExecutor = new SystemExecutorLocal();

        if (nativeTarget.getOperatingSystem() == OperatingSystem.LINUX || nativeTarget.getOperatingSystem() == OperatingSystem.FREEBSD) {

            // uname -a for kernel
            // e.g. Linux bmh-jjlauer-4 6.17.0-6-generic #6-Ubuntu SMP PREEMPT_DYNAMIC Tue Oct  7 13:34:17 UTC 2025 x86_64 GNU/Linux
            // e.g. FreeBSD bmh-dev-x64-freebsd15-1 15.0-ALPHA4 FreeBSD 15.0-ALPHA4 stable/15-n280334-d2b670b27f37 GENERIC amd64
            final String uname = systemExecutor.execProcess("uname", "-a");
            log.info("uname: {}", uname);

            // /etc/os-release for distro
            final String osReleaseContent = systemExecutor.catFile("/etc/os-release");
            final OsReleaseFile osReleaseFile = OsReleaseFile.parse(osReleaseContent);

            log.info("os-release: {}", osReleaseFile.getPrettyName());

        } else if (nativeTarget.getOperatingSystem() == OperatingSystem.MACOS) {

            // uname -a for kernel
            // e.g. Linux bmh-jjlauer-4 6.17.0-6-generic #6-Ubuntu SMP PREEMPT_DYNAMIC Tue Oct  7 13:34:17 UTC 2025 x86_64 GNU/Linux
            // e.g. FreeBSD bmh-dev-x64-freebsd15-1 15.0-ALPHA4 FreeBSD 15.0-ALPHA4 stable/15-n280334-d2b670b27f37 GENERIC amd64
            final String uname = systemExecutor.execProcess("uname", "-a");
            log.info("uname: {}", uname);

            // sw_vers for distro (or it could be a .plist file we simply read)
            final String swVersContent = systemExecutor.execProcess("sw_vers");

            log.info("sw_vers: {}", swVersContent);

        } else if (nativeTarget.getOperatingSystem() == OperatingSystem.WINDOWS) {

            final String currentVersionRegQuery = systemExecutor.execProcess("reg", "query", "HKLM\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion");
            log.info("currentVersionRegQuery: {}", currentVersionRegQuery);

        }
    }

}