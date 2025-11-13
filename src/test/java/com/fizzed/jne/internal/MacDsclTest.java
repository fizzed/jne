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

import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MacDsclTest {

    @Test
    public void example() throws IOException {
        String output = Resources.stringUTF8("/com/fizzed/jne/internal/MacDsclExampleOutput.txt");

        MacDscl macDscl = MacDscl.parse(output);

        assertThat(macDscl.getShell(), is(Paths.get("/bin/zsh")));
        assertThat(macDscl.getHomeDir(), is(Paths.get("/Users/jjlauer")));
        assertThat(macDscl.getRealName(), is("Joe Lauer"));
        assertThat(macDscl.getUniqueId(), is(501));
        assertThat(macDscl.getPrimaryGroupId(), is(20));
    }

    @Test
    public void macos11() throws IOException {
        String output = Resources.stringUTF8("/fixtures/platforms/macos11/exec-dscl--read-usersbuilder.txt");

        MacDscl macDscl = MacDscl.parse(output);

        assertThat(macDscl.getShell(), is(Paths.get("/bin/zsh")));
        assertThat(macDscl.getHomeDir(), is(Paths.get("/Users/builder")));
        assertThat(macDscl.getRealName(), is("builder"));
        assertThat(macDscl.getUniqueId(), is(502));
        assertThat(macDscl.getPrimaryGroupId(), is(20));
    }

    @Test
    public void macos15() throws IOException {
        String output = Resources.stringUTF8("/fixtures/platforms/macos15/exec-dscl--read-usersbuilder.txt");

        MacDscl macDscl = MacDscl.parse(output);

        assertThat(macDscl.getShell(), is(Paths.get("/bin/zsh")));
        assertThat(macDscl.getHomeDir(), is(Paths.get("/Users/builder")));
        assertThat(macDscl.getRealName(), is("Builder"));
        assertThat(macDscl.getUniqueId(), is(502));
        assertThat(macDscl.getPrimaryGroupId(), is(20));
    }

}
