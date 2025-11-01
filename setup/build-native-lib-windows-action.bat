@setlocal ENABLEDELAYEDEXPANSION
@echo OFF

set BASEDIR=%~dp0
cd %BASEDIR%\..
set PROJECTDIR=%CD%

set BUILDOS=%1
set BUILDARCH=%2

if "%BUILDOS%"=="" (
  echo Usage: script [buildos] [buildarch]
  exit /B
)

if "%BUILDARCH%"=="" (
  echo Usage: script [buildos] [buildarch]
  exit /B
)

echo
echo Project Dir: %PROJECTDIR%
echo Target OS: %BUILDOS%
echo Target Arch: %BUILDARCH%
echo

set VCVARSALLARCH=x64
if "%BUILDARCH%"=="arm64" (
  set VCVARSALLARCH=x64_arm64
)

@REM can we install visual studio variables?
@call "C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvarsall.bat" %VCVARSALLARCH%

mkdir target
@REM -a or -t to rsync caused permissions issues on build
rsync -vr --delete ./native/ ./target/

set

cd .\target\jcat
nmake -f VCMakefile

cd ..\libhelloj
nmake -f VCMakefile