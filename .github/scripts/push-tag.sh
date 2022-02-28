#!/bin/bash
set -ev

# Prepare release
PREVIOUS_RELEASE_TAG=$(git describe --abbrev=0)
if [[ $PREVIOUS_RELEASE_TAG =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  # Set current released version
  mvn -B -q versions:set -DnewVersion=$PREVIOUS_RELEASE_TAG

  # Increment patch version
  mvn -B -q build-helper:parse-version versions:set \
  -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.nextIncrementalVersion} \
  versions:commit
else
  echo "Cannot parse the latest release tag: ${PREVIOUS_RELEASE_TAG}"
  exit 1
fi

RELEASE_TAG=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)

git config user.name "GitHub CI"
git config user.email "ARTIFACT_SERVICE_SUPPORT@here.com"

git tag -a "${RELEASE_TAG}" -m "Release ${RELEASE_TAG} from build ${GITHUB_JOB}"

git push origin --tags
