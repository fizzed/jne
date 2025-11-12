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

import com.fizzed.crux.util.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;

class WindowsRegistryTest {

    @Test @EnabledOnOs(OS.WINDOWS)
    public void queryUserEnvironmentVariables() throws Exception {
        final WindowsRegistry wr = WindowsRegistry.queryUserEnvironmentVariables(new SystemExecutorLocal());
        assertThat(wr.getValues().size(), greaterThan(1));
    }

    @Test @EnabledOnOs(OS.WINDOWS)
    public void querySystemEnvironmentVariables() throws Exception {
        final WindowsRegistry wr = WindowsRegistry.querySystemEnvironmentVariables(new SystemExecutorLocal());
        assertThat(wr.getValues().size(), greaterThan(1));
    }

    @Test
    public void parseUserQuery() throws IOException {
        String output = Resources.stringUTF8("/com/fizzed/jne/internal/WindowsEnvUserRegQuery.txt");

        final WindowsRegistry wr = WindowsRegistry.parse(output);

        assertThat(wr.getValues(), aMapWithSize(6));
        assertThat(wr.getValues(), hasEntry("Path", "C:\\Opt\\bin;%PATH%"));
        assertThat(wr.getValues(), hasEntry("TEMP", "%USERPROFILE%\\AppData\\Local\\Temp"));
        assertThat(wr.getValues(), hasEntry("TMP", "%USERPROFILE%\\AppData\\Local\\Temp"));
        assertThat(wr.getValues(), hasEntry("OneDrive", "C:\\Users\\jjlauer\\OneDrive"));
        assertThat(wr.getValues(), hasEntry("TEST", "Hello"));

        // should also be case insensitive too
        assertThat(wr.getValues().get("Test"), is("Hello"));
    }

    @Test
    public void parseSystemQuery() throws IOException {
        String output = Resources.stringUTF8("/com/fizzed/jne/internal/WindowsEnvSystemRegQuery.txt");

        final WindowsRegistry wr = WindowsRegistry.parse(output);

        assertThat(wr.getValues(), aMapWithSize(18));
        assertThat(wr.getValues(), hasEntry("Path", "C:\\Program Files\\Zulu\\zulu-21\\bin\\;C:\\Program Files\\Zulu\\zulu-17\\bin\\;C:\\Program Files\\Zulu\\zulu-11\\bin\\;C:\\Program Files\\Zulu\\zulu-8\\bin\\;C:\\WINDOWS\\system32;C:\\WINDOWS;C:\\WINDOWS\\System32\\Wbem;C:\\WINDOWS\\System32\\WindowsPowerShell\\v1.0\\;C:\\WINDOWS\\System32\\OpenSSH\\;C:\\Opt\\apache-maven-3.9.5\\bin;C:\\Program Files\\RedHat\\Podman\\;C:\\Program Files\\Git\\cmd;C:\\Program Files\\Go\\bin;C:\\Program Files\\Go\\bin;C:\\Program Files\\dotnet\\;C:\\Program Files\\PowerShell\\7\\"));
        assertThat(wr.getValues(), hasEntry("TEMP", "%SystemRoot%\\TEMP"));
        assertThat(wr.getValues(), hasEntry("TMP", "%SystemRoot%\\TEMP"));
        assertThat(wr.getValues(), hasEntry("PROCESSOR_IDENTIFIER", "Intel64 Family 6 Model 183 Stepping 1, GenuineIntel"));
        assertThat(wr.getValues(), hasEntry("TEST", "Hello"));

        // should also be case insensitive too
        assertThat(wr.getValues().get("Test"), is("Hello"));
    }

    @Test
    public void parseCurrentVersion11Query() throws IOException {
        String output = Resources.stringUTF8("/com/fizzed/jne/internal/WindowsCurrentVersionRegQuery11.txt");

        final WindowsRegistry wr = WindowsRegistry.parse(output);

        assertThat(wr.getValues(), aMapWithSize(32));
        assertThat(wr.getValues(), hasEntry("DisplayVersion", "25H2"));
        assertThat(wr.getValues(), hasEntry("ProductId", "00330-80000-00000-AA238"));
        assertThat(wr.getValues(), hasEntry("UBR", "6901"));
        assertThat(wr.getValues(), hasEntry("CurrentBuildNumber", "26200"));
        assertThat(wr.getValues(), hasEntry("InstallDate", "1730175994"));
        assertThat(wr.getValues(), hasEntry("LCUVer", "10.0.26100.6901"));
        assertThat(wr.getValues(), hasEntry("BuildLabEx", "26100.1.amd64fre.ge_release.240331-1435"));

        // should also be case insensitive too
        assertThat(wr.getValues().get("displayversion"), is("25H2"));
    }

    @Test
    public void parseQueryWithNewlinesAtStartButNotAtEnd() throws IOException {
        String output = "" +
            "\n" +
            "\n" +
            "\n" +
            "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\n" +
            "    SystemRoot    REG_SZ    C:\\WINDOWS\n" +
            "    BaseBuildRevisionNumber    REG_DWORD    0x1";

        final WindowsRegistry wr = WindowsRegistry.parse(output);

        assertThat(wr.getValues(), aMapWithSize(2));
        assertThat(wr.getValues(), hasEntry("SystemRoot", "C:\\WINDOWS"));
        assertThat(wr.getValues(), hasEntry("BaseBuildRevisionNumber", "1"));
    }

    @Test
    public void parseQueryWithNewlinesAtEnd() throws IOException {
        String output = "" +
            "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\n" +
            "    SystemRoot    REG_SZ    C:\\WINDOWS\n" +
            "    BaseBuildRevisionNumber    REG_DWORD    0x1\n" +
            "    Key REG_SZ" +
            "\n" +
            "\n";

        final WindowsRegistry wr = WindowsRegistry.parse(output);

        assertThat(wr.getValues(), aMapWithSize(3));
        assertThat(wr.getValues(), hasEntry("SystemRoot", "C:\\WINDOWS"));
        assertThat(wr.getValues(), hasEntry("BaseBuildRevisionNumber", "1"));
        assertThat(wr.getValues(), hasEntry("Key", null));
    }

}