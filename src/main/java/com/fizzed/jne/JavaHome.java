package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2023 Fizzed, Inc
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class JavaHome {
    static private final Logger log = LoggerFactory.getLogger(JavaHome.class);

    final private Path directory;
    final private Path javaExe;
    final private Path javacExe;
    final private Path nativeImageExe;
    final private OperatingSystem operatingSystem;
    final private HardwareArchitecture hardwareArchitecture;
    final private ABI abi;
    final private String vendor;
    final private JavaDistribution distribution;
    final private JavaVersion version;
    final private Map<String,String> releaseProperties;

    JavaHome(Path directory, Path javaExe, Path javacExe, Path nativeImageExe, OperatingSystem operatingSystem,
             HardwareArchitecture hardwareArchitecture, ABI abi, String vendor, JavaDistribution distro, JavaVersion version,
             Map<String, String> releaseProperties) {
        this.directory = directory;
        this.javaExe = javaExe;
        this.javacExe = javacExe;
        this.nativeImageExe = nativeImageExe;
        this.operatingSystem = operatingSystem;
        this.hardwareArchitecture = hardwareArchitecture;
        this.abi = abi;
        this.vendor = vendor;
        this.distribution = distro;
        this.version = version;
        this.releaseProperties = releaseProperties;
    }

    public Path getDirectory() {
        return directory;
    }

    public Path getJavaExe() {
        return javaExe;
    }

    public Path getJavacExe() {
        return javacExe;
    }

    public Path getNativeImageExe() {
        return nativeImageExe;
    }

    public JavaImageType getImageType() {
        if (this.nativeImageExe != null) {
            return JavaImageType.NIK;
        } else if (this.javacExe != null) {
            return JavaImageType.JDK;
        } else {
            return JavaImageType.JRE;
        }
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public HardwareArchitecture getHardwareArchitecture() {
        return hardwareArchitecture;
    }

    public ABI getAbi() {
        return abi;
    }

    public String getVendor() {
        return vendor;
    }

    public JavaDistribution getDistribution() {
        return distribution;
    }

    public JavaVersion getVersion() {
        return version;
    }

    public Map<String, String> getReleaseProperties() {
        return releaseProperties;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (this.distribution != null) {
            sb.append(this.distribution.getDescriptor());
        }
        if (sb.length() > 0) {
            sb.append(" ");
        }
        sb.append(this.getImageType());
        sb.append(" ").append(this.version);
        sb.append(" (");
        if (this.operatingSystem != null) {
            sb.append(this.operatingSystem.getDescriptor());
            if (this.hardwareArchitecture != null) {
                sb.append(", ");
                sb.append(this.hardwareArchitecture.getDescriptor());
            }
            sb.append(", ");
        }
        sb.append(this.directory);
        sb.append(")");
        return sb.toString();
    }

    static public JavaHome current() throws IOException {
        Path javaHomeDir = Paths.get(System.getProperty("java.home"));
        return current(javaHomeDir);
    }

    // for testing
    static JavaHome current(Path javaHomeDir) throws IOException {
        return JavaHomes.detect(javaHomeDir, false, JavaHomes.CURRENT_JVM_RELEASE_PROPERTIES_PROVIDER);
    }

}
