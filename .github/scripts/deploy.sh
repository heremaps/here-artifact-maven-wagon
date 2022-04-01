#!/bin/bash
set -ev

export GPG_TTY=$(tty)

gpg --version

# Import gpg key
echo $GPG_PRIVATE_KEY | base64 -d > private.key
gpg --import --batch private.key

# Deploy to Maven Central
mvn --settings .github/settings.xml clean deploy -Prelease
