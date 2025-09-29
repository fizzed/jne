package com.fizzed.jne.internal;

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

import javax.crypto.Mac;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Arrays.asList;

public class MacDscl {
    static private final Logger log = LoggerFactory.getLogger(MacDscl.class);

    // for now this is actually all we care about
    private Path homeDir;
    private Path shell;
    private String realName;
    private Integer uniqueId;
    private Integer primaryGroupId;

    public Path getHomeDir() {
        return homeDir;
    }

    public Path getShell() {
        return shell;
    }

    public String getRealName() {
        return realName;
    }

    public Integer getUniqueId() {
        return uniqueId;
    }

    public Integer getPrimaryGroupId() {
        return primaryGroupId;
    }

    static public MacDscl readByUser(String user) throws Exception {
        // we need to find the user's home directory, basically on mac it should always be in /Users
        final Path usersDir = Paths.get("/Users");
        if (!Files.exists(usersDir)) {
            log.warn("{} was expected to exist, but is missing, unable to lookup user {}", usersDir, user);
            return null;
        }

        final Path homeDir = usersDir.resolve(user);

        if (!Files.exists(homeDir)) {
            log.warn("Home directory {} was not found for user {}", homeDir, user);
            return null;
        }

        return readByHomeDirectory(homeDir);
    }

    static public MacDscl readByHomeDirectory(Path homeDir) throws Exception {
        // dscl . -read $homeDir
        final List<String> commands = asList("dscl", ".", "-read", homeDir.toAbsolutePath().toString());

        final String output = Utils.execAndGetOutput(commands);

        return readOutput(output);
    }

    static public MacDscl readOutput(String output) {
        MacDscl v = new MacDscl();

        // process output line by line
        int pos = 0;
        int nextNewlinePos = output.indexOf('\n', pos);
        while (nextNewlinePos > 0) {
            String line = output.substring(pos, nextNewlinePos);

            if (line.startsWith("UserShell:")) {
                v.shell = Paths.get(line.substring(10).trim());
            } else if (line.startsWith("NFSHomeDirectory:")) {
                v.homeDir = Paths.get(line.substring(17).trim());
            } else if (line.startsWith("UniqueID:")) {
                v.uniqueId = Integer.valueOf(line.substring(9).trim());
            } else if (line.startsWith("PrimaryGroupID:")) {
                v.primaryGroupId = Integer.valueOf(line.substring(15).trim());
            } else if (line.startsWith("RealName:")) {
                // this one is interesting, because we actually want the next line
                int _nextNewLinePos = output.indexOf('\n', nextNewlinePos+1);
                if (_nextNewLinePos > 0) {
                    v.realName = output.substring(nextNewlinePos+1, _nextNewLinePos).trim();
                }
            }

            pos = nextNewlinePos + 1;
            nextNewlinePos = output.indexOf('\n', pos);
        }

        return v;
    }

}
