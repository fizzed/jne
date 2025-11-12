package com.fizzed.jne.internal;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class SystemExecutorLocalContainer extends SystemExecutorLocal {

    private final String containerExe;
    private final String containerImage;

    public SystemExecutorLocalContainer(String containerExe, String containerImage) {
        this.containerExe = containerExe;
        this.containerImage = containerImage;
    }

    @Override
    public String catFile(String file) throws Exception {
        return this.execProcess("cat", file);
    }

    @Override
    public String execProcess(List<Integer> exitValues, String... command) throws Exception {
        // build a new array of commands
        List<String> newCommands = new ArrayList<>();
        newCommands.addAll(asList(this.containerExe, "run", this.containerImage));
        newCommands.addAll(asList(command));
        return super.execProcess(exitValues, newCommands.toArray(new String[0]));
    }

}