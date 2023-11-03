package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2023 Fizzed, Inc
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

import java.util.List;

class JavaHomesDemo {
    static private final Logger log = LoggerFactory.getLogger(JavaHomesDemo.class);

    static public void main(String[] args) throws Exception {
        final List<JavaHome> javaHomes = JavaHomes.detect();

        for (JavaHome javaHome : javaHomes) {
            log.info("  was java home");
            log.info("    javaExe: {}", javaHome.getJavaExe());
            log.info("    javacExe: {}", javaHome.getJavacExe());
            log.info("    version: {}", javaHome.getVersion());
            log.info("      major: {}", javaHome.getVersion().getMajor());
            log.info("      minor: {}", javaHome.getVersion().getMinor());
            log.info("      security: {}", javaHome.getVersion().getSecurity());
            log.info("    os: {}", javaHome.getOperatingSystem());
            log.info("    arch: {}", javaHome.getHardwareArchitecture());
            log.info("    vendor: {}", javaHome.getVendor());
            log.info("    releaseProperties:");
            javaHome.getReleaseProperties().forEach((k, v) -> {
                if ("MODULES".equals(k) || "COMMIT_INFO".equals(k) || "SOURCE".equals(k)) {
                    return;
                }
                log.info("      {} -> {}", k, v);
            });
        }

        // hello daddy
        // mom is cute
    }

}
