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

/**
 * https://openjdk.org/jeps/223
 * https://hg.openjdk.org/jdk9/jdk9/jdk/file/tip/src/java.base/share/native/include/jvm.h
 *
 * $MAJOR   (where missing values are zero by default)
 * $MAJOR.$MINOR.$SECURITY
 * $MAJOR.$MINOR.$SECURITY.$BUILD
 *
 * java -version output:
 *
 * openjdk version \"${java.version}\"
 * ${java.runtime.name} (build ${java.runtime.version})
 * ${java.vm.name} (build ${java.vm.version}, ${java.vm.info})
 */
public class JavaVersion {

    final private String source;
    final private int major;
    final private int minor;
    final private int security;
    final private int build;

    public JavaVersion(String source, int major, int minor, int security, int build) {
        this.source = source;
        this.major = major;
        this.minor = minor;
        this.security = security;
        this.build = build;
    }

    public String getSource() {
        return source;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getSecurity() {
        return security;
    }

    public int getBuild() {
        return build;
    }

    public String toJavaVersion() {
        if (this.major < 9) {
            return "1." + this.major + "." + this.minor + "_" + this.security;
        } else {
            return this.toSemanticVersion();
        }
    }

    public String toSemanticVersion() {
        if (this.build > 0) {
            return this.major + "." + this.minor + "." + this.security + "." + this.build;
        } else {
            return this.major + "." + this.minor + "." + this.security;
        }
    }

    @Override
    public String toString() {
        return this.toSemanticVersion();
    }

    static public JavaVersion current() {
        String version = System.getProperty("java.version");
        return parse(version);
    }

    static public JavaVersion parse(String version) {
        if (version == null) {
            throw new IllegalArgumentException("java version was null");
        }

        int major = 0;
        int minor = 0;
        int security = 0;
        int build = 0;

        try {
            String sourceVersion = version;

            // pre-Java 9 version number
            if (version.startsWith("1.")) {
                // e.g. 1.8.0_352 which we normalize to the modern format so we can use its parsing routing
                sourceVersion = version.substring(2).replace('_', '.').replace('-', '.');
            }

            final int len = sourceVersion.length();
            int periodPos1 = sourceVersion.indexOf('.');
            major = Integer.parseInt(sourceVersion.substring(0, periodPos1 > 0 ? periodPos1 : len));
            if (periodPos1 > 0 && periodPos1 < len-1) {
                int periodPos2 = sourceVersion.indexOf('.', periodPos1+1);
                minor = Integer.parseInt(sourceVersion.substring(periodPos1+1, periodPos2 > 0 ? periodPos2 : len));
                if (periodPos2 > 0 && periodPos2 < len-1) {
                    int periodPos3 = sourceVersion.indexOf('.', periodPos2+1);
                    security = Integer.parseInt(sourceVersion.substring(periodPos2+1, periodPos3 > 0 ? periodPos3 : len));
                    if (periodPos3 > 0 && periodPos3 < len-1) {
                        // e.g. 9.0.1.1
                        build = Integer.parseInt(sourceVersion.substring(periodPos3+1));
                    } else {
                        // e.g. 9.0.1
                    }
                } else {
                    // e.g. 9.0
                }
            } else {
                // e.g. 9
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("java version [" + version + "] invalid format: not of X.X.X", e);
        }

        return new JavaVersion(version, major, minor, security, build);
    }

}
