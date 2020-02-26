#!/bin/bash
set -ev

# Prepare release
mvn -B -q build-helper:parse-version versions:set \
  -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.incrementalVersion} \
  versions:commit

RELEASE_VERSION=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)
GIT_RELEASE_TAG="Release-${RELEASE_VERSION}"

sed -i "s/<tag>.*<\/tag>/<tag>${GIT_RELEASE_TAG}<\/tag>/g" pom.xml
find . -name "pom.xml" | xargs git add

git config user.name "Travis CI"
git config user.email "OLP_ENG_LIMERICK@here.com"

git commit -m "[skip ci] Preparing for release $RELEASE_VERSION"
git tag -a "${GIT_RELEASE_TAG}" -m "Release $RELEASE_VERSION from build $TRAVIS_BUILD_ID"