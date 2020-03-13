#!/bin/bash
set -ev

# Prepare release
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