#!/bin/bash
set -ev

# Import gpg key
gpg --fast-import .travis/codesigning.asc
# Deploy to Maven Central
mvn --settings .travis/travis-settings.xml clean deploy -Prelease