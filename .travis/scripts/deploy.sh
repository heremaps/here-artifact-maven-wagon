#!/bin/bash
set -ev

# Decrypt gpg key
openssl aes-256-cbc -K $encrypted_4f116abdd4b6_key -iv $encrypted_4f116abdd4b6_iv -in .travis/codesigning.asc.enc -out .travis/codesigning.asc -d
# Import gpg key
gpg --fast-import .travis/codesigning.asc
# Deploy to Maven Central
mvn --settings .travis/travis-settings.xml clean deploy -Prelease