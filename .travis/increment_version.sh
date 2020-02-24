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
git commit -m "Updating to ${NEXT_VERSION}-SNAPSHOT after releasing $MAIN_VERSION"

# Output environment variables
cat >> environment.properties << EOF
MAIN_VERSION=${MAIN_VERSION}
GIT_HEAD_COMMIT=$(git rev-parse HEAD)
EOF

# TEMP
git log -2
git tag -n9
cat environment.properties