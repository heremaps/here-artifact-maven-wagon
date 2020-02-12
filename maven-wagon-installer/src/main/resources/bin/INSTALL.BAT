echo off
REM Copyright (C) 2019 HERE Global B.V. and its affiliate(s).
REM All rights reserved.
echo on
call mvn install:install-file -Dfile="artifact-wagon-${artifact.wagon.version}.jar" -DpomFile="artifact-wagon-${artifact.wagon.version}.pom"
echo on
call mvn install:install-file -Dfile="maven-parent-${artifact.wagon.version}.pom" -DpomFile="maven-parent-${artifact.wagon.version}.pom"
