package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2025 Fizzed, Inc
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

import static java.util.Arrays.asList;

public class InstallEnvironmentDemo {
    static private final Logger log = LoggerFactory.getLogger(InstallEnvironmentDemo.class);

    static public void main(String[] args) throws Exception {
        InstallEnvironment ie = InstallEnvironment.detect("Apache Maven", "maven");

        log.info("unitName: {}", ie.getUnitName());
        log.info("applicationName: {}", ie.getApplicationName());
        log.info("");
        log.info("applicationRootDir: {}", ie.getApplicationRootDir());
        log.info("systemRootDir: {}", ie.getSystemRootDir());
        log.info("localRootDir: {}", ie.getLocalRootDir());
        log.info("");
        log.info("applicationDir: {}", ie.getApplicationDir());
        log.info("systemBinDir: {}", ie.getSystemBinDir());
        log.info("systemShareDir: {}", ie.getSystemShareDir());

        log.info("localApplicationDir: {}", ie.getLocalApplicationDir());
        log.info("optApplicationDir: {}", ie.getOptApplicationDir());
        log.info("localBinDir: {}", ie.getLocalBinDir());
        log.info("localShareDir: {}", ie.getLocalShareDir());

        // windows: reg query HKEY_CURRENT_USER\Environment
        // windows: reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment"

        ie.installEnv(
            UserEnvironment.detectLogical(),
            InstallEnvironment.EnvScope.USER,
            asList(new InstallEnvironment.EnvVar("TEST","Hello")),
            asList(new InstallEnvironment.EnvPath(true, Paths.get("C:\\Opt\\bin"))));
    }

}
