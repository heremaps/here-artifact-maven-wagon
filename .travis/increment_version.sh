#!/bin/bash
set -ev

echo "Incrementing version"
MAIN_VERSION=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)

mvn -B build-helper:parse-version versions:set \
  -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.nextIncrementalVersion}-SNAPSHOT \
  versions:commit

NEXT_VERSION=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)

sed -i "s/<tag>.*<\/tag>/<tag>HEAD<\/tag>/g" pom.xml
find . -name "pom.xml" -not -path "./maven-wagon-installer/src/*" -not -path "./maven-wagon-installer/target/*" \
  | xargs git add
git commit -m "[skip ci] Updating to ${NEXT_VERSION}-SNAPSHOT after releasing $MAIN_VERSION"