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

import com.fizzed.jne.internal.EtcPasswd;
import com.fizzed.jne.internal.MacDscl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class UserEnvironmentDemo {
    static private final Logger log = LoggerFactory.getLogger(UserEnvironmentDemo.class);

    static public void main(String[] args) throws Exception {

        //MacDscl.read(Paths.get("/Users/jjlauer"));

//        UserEnvironment userEnvironment = UserEnvironment.detectLogical();
        UserEnvironment userEnvironment = UserEnvironment.detectEffective();

        log.info("user: {}", userEnvironment.getUser());
        log.info("elevated: {}", userEnvironment.getElevated());
        log.info("displayName: {}", userEnvironment.getDisplayName());
        log.info("userId: {}", userEnvironment.getUserId());
        log.info("groupId: {}", userEnvironment.getGroupId());
        log.info("homeDir: {}", userEnvironment.getHomeDir());
        log.info("shell: {}", userEnvironment.getShell());
        log.info("shellType: {}", userEnvironment.getShellType());

        /*EtcPasswd etcPasswd = EtcPasswd.detect();

        etcPasswd.getEntries().forEach(entry -> {
            log.info("{}, home={}, shell={}", entry.getUsername(), entry.getHome(), entry.getShell());
        });*/

    }

}
