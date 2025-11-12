package com.fizzed.jne;

import com.fizzed.jne.internal.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PlatformFixtureGenerator {
    static private final Logger log = LoggerFactory.getLogger(PlatformFixtureGenerator.class);

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

        // real hosts

        /*final String fixtureName = "windows11";
        final String host = "bmh-build-x64-windows-latest";*/

        /*final String fixtureName = "windows10";
        final String host = "bmh-build-x64-win10-1";*/

        /*final String fixtureName = "windows7";
        final String host = "bmh-build-x64-win7-1";*/

        /*final String fixtureName = "freebsd13";
        final String host = "bmh-build-x64-freebsd-baseline";*/

        /*final String fixtureName = "macos11";
        final String host = "bmh-build-x64-macos11-1";*/

        /*final String fixtureName = "macos15";
        final String host = "bmh-build-x64-macos15-1";*/

        /*final String fixtureName = "fedora43";
        final String host = "bmh-dev-x64-fedora43-1";*/

        /*final String fixtureName = "ubuntu2510";
        final String host = "localhost";*/

        /*final String fixtureName = "freebsd13";
        final String host = "bmh-build-x64-freebsd13-1";*/

        /*final String fixtureName = "openbsd78";
        final String host = "bmh-build-x64-openbsd78-1";*/

        final String fixtureName = "alpine315";
        final String host = "bmh-build-x64-alpine315-1";

        final SystemExecutor systemExecutor = new SystemExecutorSsh(host);




        final Path fixtureRootDir = Paths.get("src/test/resources/fixtures/platforms");
        final Path fixtureDir = fixtureRootDir.resolve(fixtureName);
        Files.createDirectories(fixtureDir);
        final SystemExecutorFixture fixtureExecutor = new SystemExecutorFixture(fixtureDir, systemExecutor);

        final PlatformInfo platformInfoAll = PlatformInfo.detectAll(fixtureExecutor);
        // additional execs so we have that data for other tests too
        EtcPasswd.detect(fixtureExecutor);
        // builder we don't care about, nothing is sensitive
        try {
            MacDscl.readByHomeDirectory(Paths.get("/Users/builder"), fixtureExecutor);
        } catch (Exception e) { /* ignore */ }

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
