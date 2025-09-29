package com.fizzed.jne.internal;

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