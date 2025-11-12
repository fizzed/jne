package com.fizzed.jne.internal;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class RemoteSshSystemExecutor extends LocalSystemExecutor {

    private final String host;

    public RemoteSshSystemExecutor(String host) {
        this.host = host;
    }

    @Override
    public String catFile(String file) throws Exception {
        return this.execProcess("cat", file);
    }

    @Override
    public String execProcess(List<Integer> exitValues, String... command) throws Exception {
        // build a new array of commands
        List<String> newCommands = new ArrayList<>();
        newCommands.addAll(asList("ssh", this.host));
        newCommands.addAll(asList(command));
        return super.execProcess(exitValues, newCommands.toArray(new String[0]));
    }

}