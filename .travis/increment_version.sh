#!/bin/bash
set -ev

# Increment version and push to remote before releasing to Maven Central to eliminame multiple commit race conditions
# If push to removte fails - this means that we are not at the latest commit anymore and should abort
RELEASE_VERSION=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)

mvn -B -q build-helper:parse-version versions:set \
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
git commit -m "[skip ci] Updating to ${NEXT_VERSION}-SNAPSHOT after releasing $RELEASE_VERSION"

# Push to GitHub
GIT_RELEASE_TAG=$(git describe --abbrev=0)

git remote add origin-travis https://${GITHUB_TOKEN}@${GITHUB_REPO}
git push origin-travis HEAD:${TRAVIS_BRANCH}
git push origin-travis "${GIT_RELEASE_TAG}"

# Reset to previous commit for release
git reset --hard "${GIT_RELEASE_TAG}"