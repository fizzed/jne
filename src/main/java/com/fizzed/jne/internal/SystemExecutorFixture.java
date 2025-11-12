package com.fizzed.jne.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class SystemExecutorFixture implements SystemExecutor {
    private final Logger log = LoggerFactory.getLogger(SystemExecutorFixture.class);

    final Path fixtureDir;
    final SystemExecutor underlyingExecutor;

    /**
     * Reading mode
     * @param fixtureDir
     */
    public SystemExecutorFixture(Path fixtureDir) {
        this.fixtureDir = fixtureDir;
        this.underlyingExecutor = null;
    }

    /**
     * Writing mode
     * @param fixtureDir
     * @param underlyingExecutor
     */
    public SystemExecutorFixture(Path fixtureDir, SystemExecutor underlyingExecutor) {
        this.fixtureDir = fixtureDir;
        this.underlyingExecutor = underlyingExecutor;
    }

    static public String cleanName(String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    static public String createName(String name, String... otherNames) {
        StringBuilder sb = new StringBuilder();
        sb.append(cleanName(name));
        for (String otherName : otherNames) {
            sb.append("-").append(cleanName(otherName));
        }
        sb.append(".txt");
        return sb.toString();
    }

    private String processFixtureIO(String name, String output) throws Exception {
        Path fixtureFile = this.fixtureDir.resolve(name);
        if (this.underlyingExecutor != null) {
            Files.write(fixtureFile, output.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.debug("Writing fixture file: {}", fixtureFile);
            return output;
        } else {
            log.debug("Reading fixture file: {}", fixtureFile);
            final byte[] bytes = Files.readAllBytes(fixtureFile);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    @Override
    public String catFile(String file) throws Exception {
        String name = createName("cat", file);
        String output = this.underlyingExecutor != null ? this.underlyingExecutor.catFile(file) : null;
        return processFixtureIO(name, output);
    }

    @Override
    public String execProcess(List<Integer> exitValues, String... command) throws Exception {
        String name = createName("exec", command);
        String output = this.underlyingExecutor != null ? this.underlyingExecutor.execProcess(exitValues, command) : null;
        return processFixtureIO(name, output);
    }

}