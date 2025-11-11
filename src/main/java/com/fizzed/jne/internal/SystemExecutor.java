package com.fizzed.jne.internal;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Generic and very simple interface for getting information from a system, so that we can use it locally, mock in tests,
 * or even support it via ssh, containers, etc.
 */
public interface SystemExecutor {

    default String catFile(Path file) throws Exception {
        return catFile(file.toString());
    }

    String catFile(String file) throws Exception;

    default String execProcess(String... command) throws Exception {
        return execProcess(Collections.singletonList(0), command);
    }

    String execProcess(List<Integer> exitValues, String... command) throws Exception;

}