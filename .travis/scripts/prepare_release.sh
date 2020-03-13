#!/bin/bash
set -ev

# Prepare release
LATEST_RELEASE_TAG=$(git describe --abbrev=0)
if [[ $LATEST_RELEASE_TAG =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  # Set current released version
  mvn -B -q versions:set -DnewVersion=$LATEST_RELEASE_TAG

  # Increment patch version
  mvn -B -q build-helper:parse-version versions:set \
  -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.nextIncrementalVersion} \
  versions:commit
else
  echo "Cannot parse the latest release tag: ${LATEST_RELEASE_TAG}"
  exit 1
fi

RELEASE_VERSION=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)

git config user.name "Travis CI"
git config user.email "OLP_ENG_LIMERICK@here.com"

git tag -a "${RELEASE_VERSION}" -m "Release $RELEASE_VERSION from build $TRAVIS_BUILD_ID"

git remote add origin-travis https://${GITHUB_TOKEN}@${GITHUB_REPO}
git push origin-travis "${RELEASE_VERSION}"