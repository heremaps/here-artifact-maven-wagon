#!/bin/bash
set -ev

gpg --version

# Import gpg key
echo $GPG_PRIVATE_KEY | base64 -d > private.key
gpg --import --batch private.key

# Deploy to Maven Central
mvn --settings .github/settings.xml clean deploy -Prelease
