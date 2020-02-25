#!/bin/bash
set -ev

echo "Pushing to GitHub"
GIT_RELEASE_TAG=$(git describe --abbrev=0)

git remote add origin-travis https://${GITHUB_TOKEN}@${GITHUB_REPO}
git push origin-travis "${GIT_RELEASE_TAG}"
git push origin-travis HEAD:travis-test