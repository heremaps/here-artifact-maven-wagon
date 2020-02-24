#!/bin/bash
set -ev

echo "Pushing to GitHub"
git config user.name "Travis CI"
git config user.email "OLP_ENG_LIMERICK@here.com"

git remote add origin-travis https://${GITHUB_TOKEN}@github.com/heremaps/here-artifact-maven-wagon.git
GIT_RELEASE_TAG=$(git describe --abbrev=0)
echo "${GIT_RELEASE_TAG}"
# git push origin-travis "${GIT_RELEASE_TAG}"
# git push origin-travis HEAD:travis-test