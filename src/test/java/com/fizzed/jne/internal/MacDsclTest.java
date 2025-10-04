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
    public void parseOutput() throws IOException {
        String output = Resources.stringUTF8("/com/fizzed/jne/internal/MacDsclExampleOutput.txt");

        MacDscl macDscl = MacDscl.readOutput(output);

        assertThat(macDscl.getShell(), is(Paths.get("/bin/zsh")));
        assertThat(macDscl.getHomeDir(), is(Paths.get("/Users/jjlauer")));
        assertThat(macDscl.getRealName(), is("Joe Lauer"));
        assertThat(macDscl.getUniqueId(), is(501));
        assertThat(macDscl.getPrimaryGroupId(), is(20));
    }
}
