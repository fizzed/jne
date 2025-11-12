package com.fizzed.jne;

import com.fizzed.jne.internal.SystemExecutor;
import com.fizzed.jne.internal.SystemExecutorFixture;
import com.fizzed.jne.internal.SystemExecutorSsh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PlatformInfoFixtureGenerator {
    static private final Logger log = LoggerFactory.getLogger(PlatformInfoFixtureGenerator.class);

    static public void main(String[] args) throws Exception {
//        final PlatformInfo platformInfoAll = PlatformInfo.detectAll();

        // detect stuff via a container :-)
//        final String containerImage = "docker.io/alpine:3.10";                        // musl
//        final String containerImage = "ghcr.io/void-linux/void-musl:latest";          // musl
//        final String containerImage = "docker.io/chimeralinux/chimera:latest";          // musl

        /*final String fixtureName = "ubuntu1604";
        final String containerImage = "docker.io/ubuntu:16.04";*/

        /*final String fixtureName = "ubuntu1804";
        final String containerImage = "docker.io/ubuntu:18.04";*/

        /*final String fixtureName = "fedora43";
        final String containerImage = "docker.io/fedora:43";*/

        /*final String fixtureName = "alpine312";
        final String containerImage = "docker.io/alpine:3.12";*/


//        final String containerImage = "docker.io/ubuntu:18.04";
//        final String containerImage = "docker.io/ubuntu:20.04";

//        final SystemExecutor systemExecutor = new SystemExecutorLocalContainer("podman", containerImage);


//        final String host = "bmh-build-x64-windows-baseline";

        /*final String fixtureName = "windows11";
        final String host = "bmh-build-x64-windows-latest";*/

        /*final String fixtureName = "freebsd13";
        final String host = "bmh-build-x64-freebsd-baseline";*/

        final String fixtureName = "macos11";
        final String host = "bmh-build-x64-macos11-1";

//        final String host = "bmh-build-x64-openbsd-baseline";
//        final String host = "bmh-build-x64-openbsd-latest";
//        final String host = "bmh-build-riscv64-linux-latest";
//        final String host = "bmh-build-arm64-linux-baseline";

        final SystemExecutor systemExecutor = new SystemExecutorSsh(host);




        final Path fixtureRootDir = Paths.get("src/test/resources/fixtures/platforms");
        final Path fixtureDir = fixtureRootDir.resolve(fixtureName);
        Files.createDirectories(fixtureDir);
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(fixtureDir, systemExecutor);

        final PlatformInfo platformInfoAll = PlatformInfo.detectAll(fixtureExecutor);

        log.info("");
        log.info("Platform Info (using detectAll):");
        log.info("  operatingSystem: {}", platformInfoAll.getOperatingSystem());
        log.info("  hardwareArchitecture: {}", platformInfoAll.getHardwareArchitecture());
        log.info("  name: {}", platformInfoAll.getName());
        log.info("  displayName: {}", platformInfoAll.getDisplayName());
        log.info("  version: {}", platformInfoAll.getVersion());
        log.info("  kernelVersion: {}", platformInfoAll.getKernelVersion());
        log.info("  uname: {}", platformInfoAll.getUname());
        log.info("  libC: {}", platformInfoAll.getLibC());
        log.info("  libCVersion: {}", platformInfoAll.getLibCVersion());

        log.info("");
    }

}
