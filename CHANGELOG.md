# Java Native Extractor by Fizzed

## 4.4.0 - 2025-09-19

 - Add support & runners for Java 25
 - JavaVersion class now parse additional common formats of numbers
 - Added support for PPC64 and a few more operating systems
 - Compiler flags now include "parameters" so classes like JavaVersion can be deserialized by Jackson

## 4.3.0 - 2024-12-23

 - Add support for nested JAR extraction (jar-in-jar) for locating resources (@hildo)

## 4.2.0 - 2024-12-17

 - Add new NativeLanguageModel utility class for helping build custom native lingo such as "amd64" instead of X64

## 4.1.1 - 2023-11-03

 - Add new sorted() feature for JavaHomeFinder

## 4.1.0 - 2023-11-03
 
 - New JavaVersion, JavaHome, JavaHomeFinder, and JavaDistribution utility classes for finding, detecting, and
using local Java VMs.

## 4.0.4 - 2023-10-30

 - New NativeTarget.toAutoConfTarget() feature for configuring gcc/g++ hosts and targets

## 4.0.3 - 2023-10-26

 - OperatingSystem and HardwareArchitecture enums now have human readable descriptors.

## 4.0.2 - 2023-10-25

 - Fix support for armel and armhf detection in NativeTarget for toRustTarget()

## 4.0.1 - 2023-10-24

 - Improve speed of ABI detection on non-linux operating systems.

## 4.0.0 - 2023-10-24

 - New NativeTarget utility class, which is now used under-the-hood by the JNE class.  NativeTarget can be used for 
more purposes than just loading libraries, such as helping normalizing os/archs for building rust projects, etc.

## 3.3.0 - 2023-10-18

 - Java 8+ compat moving forward
 - CI testing on Java 21

## 3.2.3 - 2023-10-18

 - Final release supporting Java 7!
 - Log message enhancements

## 3.2.2 - 2023-01-12

 - More enhancements to MUSL detection

## 3.2.1 - 2023-01-13

 - Improved ARMEL vs. ARMHF detection

## 3.1.2 - 2023-01-03

 - New MemoizedInitializer and MemoizedRunner utility classes for loading of libs with
   double locking.

## 3.1.1 - 2023-01-02

 - Remove duplicate logging of MUSL detection

## 3.1.0 - 2023-01-02

 - Major refactor and cleanup
 - Support for MUSL libc detection so "linux_musl" libs can be loaded
 - Support for additional architectures such as riscv64, armhf, etc.

## 3.0.2 - 2022-12-21

 - Support for arm64

## 3.0.1 - 2017-08-18

 - Only create temp dir a single time per JVM instance
 - Use UUID for temp dir

## 3.0.0 - 2017-07-17
 - Bump parent to v2.1.0
 - Add ANY enum for OS
 - New `findFile` feature to extract generic resources
 - Initial unit tests
 - Renamed Arch class to HardwareArchitecture
 - Renamed OS class to OperatingSystem

## 2.0.1 - 2016-08-23
 - Verify temp dir exists (mpanthony)

## 2.0.0 - 2015-12-18
 - Refactored package to com.fizzed
 - Added slf4j for logging

## 1.2.0 - 2015-12-11
 - Refactored project layout

## 1.1.0 - 2014-04-16
 - Added ability to rename extracted file (e.g. cat to mycat)

## 1.0.1 - 2014-03-18
 - Initial release
