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

import com.fizzed.jne.internal.LocalSystemExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemPlatformDemo {
    static private final Logger log = LoggerFactory.getLogger(SystemPlatformDemo.class);

    static public void main(String[] args) throws Exception {
        SystemPlatform systemPlatform = SystemPlatform.detect(new LocalSystemExecutor());

        log.info("operatingSystem: {}", systemPlatform.getOperatingSystem());
        log.info("hardwareArchitecture: {}", systemPlatform.getHardwareArchitecture());
        log.info("abi: {}", systemPlatform.getAbi());
        log.info("name: {}", systemPlatform.getName());
        log.info("prettyName: {}", systemPlatform.getPrettyName());
        log.info("version: {}", systemPlatform.getVersion());
        log.info("kernelVersion: {}", systemPlatform.getKernelVersion());
        log.info("uname: {}", systemPlatform.getUname());
    }

}
