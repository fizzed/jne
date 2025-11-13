package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2022 Fizzed, Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fizzed.jne.internal.SystemExecutorSsh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformInfoDemo {
    static private final Logger log = LoggerFactory.getLogger(PlatformInfoDemo.class);

    static public void main(String[] args) throws Exception {
        final PlatformInfo platformInfoBasic = PlatformInfo.detectBasic();

        log.info("");
        log.info("Platform Info (using detectBasic):");
        log.info("  operatingSystem: {}", platformInfoBasic.getOperatingSystem());
        log.info("  hardwareArchitecture: {}", platformInfoBasic.getHardwareArchitecture());
        log.info("  libC: {}", platformInfoBasic.getLibC());

//        final PlatformInfo platformInfoAll = PlatformInfo.detectAll();

        // detect stuff via a container :-)
//        final String containerImage = "docker.io/alpine:3.10";                        // musl
//        final String containerImage = "ghcr.io/void-linux/void-musl:latest";          // musl
//        final String containerImage = "docker.io/chimeralinux/chimera:latest";          // musl
//        final String containerImage = "docker.io/ubuntu:16.04";                         // glibc version fails :-(
//        final String containerImage = "docker.io/ubuntu:18.04";
//        final String containerImage = "docker.io/ubuntu:20.04";
//        final PlatformInfo platformInfoAll = PlatformInfo.detectAll(
//            new LocalContainerSystemExecutor("podman", containerImage));

        final String host = "bmh-build-arm64-windows-latest";
//        final String host = "bmh-build-x64-win7-1";
//        final String host = "bmh-build-x64-windows-baseline";
//        final String host = "bmh-build-x64-windows-latest";
//        final String host = "bmh-build-x64-freebsd-baseline";
//        final String host = "bmh-build-x64-openbsd-baseline";
//        final String host = "bmh-build-x64-openbsd-latest";
//        final String host = "bmh-build-riscv64-linux-latest";
//        final String host = "bmh-build-arm64-linux-baseline";
        final PlatformInfo platformInfoAll = PlatformInfo.detect(new SystemExecutorSsh(host), PlatformInfo.Detect.ALL);

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
