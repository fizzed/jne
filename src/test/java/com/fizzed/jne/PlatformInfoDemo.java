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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformInfoDemo {
    static private final Logger log = LoggerFactory.getLogger(PlatformInfoDemo.class);

    static public void main(String[] args) throws Exception {
        // do this twice to verify its only done once
        PlatformInfo.detectOperatingSystem();
        OperatingSystem os = PlatformInfo.detectOperatingSystem();

        // do this twice to verify its only done once
        PlatformInfo.detectHardwareArchitecture();
        HardwareArchitecture arch = PlatformInfo.detectHardwareArchitecture();

        // do this twice to verify its only done once
        PlatformInfo.detectLinuxLibC();
        LinuxLibC libc = PlatformInfo.detectLinuxLibC();

        log.info("OS: {}", os);
        log.info("Arch: {}", arch);
        log.info("LibC: {}", libc);
    }

}
