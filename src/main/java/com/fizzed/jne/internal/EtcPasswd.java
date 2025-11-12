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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class EtcPasswd {
    static private final Logger log = LoggerFactory.getLogger(EtcPasswd.class);

    private final List<Entry> entries;

    public EtcPasswd(List<Entry> entries) {
        this.entries = entries;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public Entry findEntryByUserName(String userName) {
        if (userName == null) {
            return null;
        }
        for (Entry entry : entries) {
            if (Objects.equals(entry.getUsername(), userName)) {
                return entry;
            }
        }
        return null;
    }

    public Entry findEntryByUserId(Integer userId) {
        if (userId == null) {
            return null;
        }
        for (Entry entry : entries) {
            if (Objects.equals(entry.getUserId(), userId)) {
                return entry;
            }
        }
        return null;
    }

    static public EtcPasswd detect() {
        return detect(SystemExecutor.LOCAL);
    }

    static public EtcPasswd detect(SystemExecutor systemExecutor) {
        try {
            String output = systemExecutor.catFile("/etc/passwd");
            return parse(output);
        } catch (Exception e) {
            // do nothing, ignore, but we should log this
            log.debug("Unable to parse etc passwd file: {}", e.getMessage());
        }

        return null;
    }

    static public EtcPasswd parse(Path file) throws IOException {
        String content = Utils.readFileToString(file);
        return parse(content);
    }

    static public EtcPasswd parse(String content) throws IOException {
        List<Entry> entries = new ArrayList<>();

        for (String line : content.split("\n")) {
            final String[] parts = line.split(":");
            if (parts.length == 7) {
                final Entry entry = new Entry();
                entry.setUsername(parts[0]);
                entry.setUserId(Integer.valueOf(parts[2]));
                entry.setGroupId(Integer.valueOf(parts[3]));
                entry.setName(parts[4]);
                entry.setHome(parts[5]);
                entry.setShell(parts[6]);
                entries.add(entry);
            }
        }

        return new EtcPasswd(entries);
    }

    static public class Entry {

        // jjlauer:x:1000:1000:Joe Lauer:/home/jjlauer:/bin/bash
        private String username;
        private Integer userId;
        private Integer groupId;
        private String name;
        private String home;
        private String shell;

        public String getUsername() {
            return username;
        }

        public Entry setUsername(String username) {
            this.username = username;
            return this;
        }

        public Integer getUserId() {
            return userId;
        }

        public Entry setUserId(Integer userId) {
            this.userId = userId;
            return this;
        }

        public Integer getGroupId() {
            return groupId;
        }

        public Entry setGroupId(Integer groupId) {
            this.groupId = groupId;
            return this;
        }

        public String getName() {
            return name;
        }

        public Entry setName(String name) {
            this.name = name;
            return this;
        }

        public String getHome() {
            return home;
        }

        public Entry setHome(String home) {
            this.home = home;
            return this;
        }

        public String getShell() {
            return shell;
        }

        public Entry setShell(String shell) {
            this.shell = shell;
            return this;
        }

    }

}
