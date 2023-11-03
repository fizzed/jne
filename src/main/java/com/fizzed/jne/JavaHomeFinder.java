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

import java.util.*;
import java.util.stream.Collectors;

public class JavaHomeFinder {

    static public final JavaDistribution[] DEFAULT_PREFERRED_DISTRIBUTIONS = new JavaDistribution[] {
        JavaDistribution.ZULU,
        JavaDistribution.LIBERICA,
        JavaDistribution.NITRO,
        JavaDistribution.TEMURIN,
        JavaDistribution.MICROSOFT,
        JavaDistribution.CORRETTO
    };

    private boolean sorted;
    private JavaImageType imageType;
    private HardwareArchitecture hardwareArchitecture;
    private Integer minVersion;
    private Integer maxVersion;
    private JavaDistribution distribution;
    private JavaDistribution[] preferredDistributions;

    public boolean isSorted() {
        return sorted;
    }

    public JavaHomeFinder sorted() {
        this.sorted = true;
        return this;
    }

    public JavaHomeFinder sorted(boolean sorted) {
        this.sorted = sorted;
        return this;
    }

    public JavaImageType getImageType() {
        return imageType;
    }

    public JavaHomeFinder jdk() {
        this.imageType = JavaImageType.JDK;
        return this;
    }

    public JavaHomeFinder jre() {
        this.imageType = JavaImageType.JRE;
        return this;
    }

    public JavaHomeFinder nik() {
        this.imageType = JavaImageType.NIK;
        return this;
    }

    public JavaHomeFinder imageType(JavaImageType imageType) {
        this.imageType = imageType;
        return this;
    }

    public HardwareArchitecture getHardwareArchitecture() {
        return hardwareArchitecture;
    }

    public JavaHomeFinder hardwareArchitecture(HardwareArchitecture hardwareArchitecture) {
        this.hardwareArchitecture = hardwareArchitecture;
        return this;
    }

    public Integer getMinVersion() {
        return minVersion;
    }

    public JavaHomeFinder minVersion(Integer minVersion) {
        this.minVersion = minVersion;
        return this;
    }

    public Integer getMaxVersion() {
        return maxVersion;
    }

    public JavaHomeFinder maxVersion(Integer maxVersion) {
        this.maxVersion = maxVersion;
        return this;
    }

    public JavaHomeFinder version(Integer version) {
        this.maxVersion = version;
        this.minVersion = version;
        return this;
    }

    public JavaDistribution getDistribution() {
        return distribution;
    }

    public JavaHomeFinder distribution(JavaDistribution distribution) {
        this.distribution = distribution;
        return this;
    }

    public JavaDistribution[] getPreferredDistributions() {
        return preferredDistributions;
    }

    public JavaHomeFinder preferredDistributions() {
        this.preferredDistributions = DEFAULT_PREFERRED_DISTRIBUTIONS;
        return this;
    }

    public JavaHomeFinder preferredDistributions(JavaDistribution... preferredDistributions) {
        this.preferredDistributions = preferredDistributions;
        return this;
    }

    @Override
    public String toString() {
        return "sorted=" + sorted +
            ", imageType=" + imageType +
            ", minVersion=" + minVersion +
            ", maxVersion=" + maxVersion +
            ", hwArch=" + hardwareArchitecture +
            ", distribution=" + distribution +
            ", preferredDistributions=" + Arrays.toString(preferredDistributions) +
            '}';
    }

    public JavaHome find() throws ResourceNotFoundException {
        final List<JavaHome> javaHomes;
        try {
            javaHomes = JavaHomes.detect();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Unable to find Java", e);
        }
        return this.find(javaHomes);
    }

    public JavaHome find(List<JavaHome> javaHomes) throws ResourceNotFoundException {
        return this.tryFind(javaHomes)
            .orElseThrow(() -> new ResourceNotFoundException("Unable to find Java with criteria " + this.toString()));
    }

    public Optional<JavaHome> tryFind() {
        final List<JavaHome> javaHomes;
        try {
            javaHomes = JavaHomes.detect();
        } catch (Exception e) {
            return Optional.empty();
        }
        return this.tryFind(javaHomes);
    }

    public Optional<JavaHome> tryFind(List<JavaHome> javaHomes) {
        if (javaHomes == null || javaHomes.isEmpty()) {
            return Optional.empty();
        }

        // the first java home is important (it should be the one running this JVM)
        final JavaHome firstJavaHome = javaHomes.get(0);

        // filter our list down by image type, version, etc. (concrete criteria)
        final List<JavaHome> filteredJavaHomes = javaHomes.stream()
            .filter(v -> this.minVersion == null || v.getVersion().getMajor() >= this.minVersion)
            .filter(v -> this.maxVersion == null || v.getVersion().getMajor() <= this.maxVersion)
            .filter(v -> this.imageType == null || v.getImageType() == this.imageType)
            .filter(v -> this.hardwareArchitecture == null || v.getHardwareArchitecture() == this.hardwareArchitecture)
            .filter(v -> this.distribution == null || v.getDistribution() == this.distribution)
            .collect(Collectors.toList());

        if (this.sorted) {
            // sort what's left by the most recent version (descending)
            filteredJavaHomes.sort((a, b) -> b.getVersion().compareTo(a.getVersion()));
        }

        // by preferred distribution?
        if (this.preferredDistributions != null) {
            for (JavaDistribution d : this.preferredDistributions) {
                for (JavaHome javaHome : filteredJavaHomes) {
                    if (javaHome.getDistribution() == d) {
                        return Optional.of(javaHome);
                    }
                }
            }
        }

        // otherwise, return the first
        return filteredJavaHomes.stream().findFirst();
    }

}
