#!/bin/bash
set -ev

echo "Preparing release"
mvn -B build-helper:parse-version versions:set \
  -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.incrementalVersion} \
  versions:commit

MAIN_VERSION=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)
GIT_RELEASE_TAG="Release-${MAIN_VERSION}"

sed -i "s/<tag>.*<\/tag>/<tag>${GIT_RELEASE_TAG}<\/tag>/g" pom.xml
find . -name "pom.xml" -not -path "./maven-wagon-installer/src/*" -not -path "./maven-wagon-installer/target/*" \
  | xargs git add
git commit -m "Preparing for release $MAIN_VERSION"
git tag -a "${GIT_RELEASE_TAG}" -m "Release $MAIN_VERSION from build $TRAVIS_JOB_ID"

# Export some variables
cat > environment.properties << EOF
MAIN_VERSION=${MAIN_VERSION}
GIT_RELEASE_TAG=${GIT_RELEASE_TAG}
EOF

# TEMP
git log -2
git tag -n9
cat environment.properties