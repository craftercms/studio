#!/bin/bash
if [[ "$TRAVIS_PULL_REQUEST" == "false" ]]; then
  mvn -q -Dmaven.javadoc.skip=true clean deploy -DcloneUi=true --settings .travis-settings.xml;
fi
if [[ "$TRAVIS_PULL_REQUEST" != "false" ]]; then
  mvn -q -Dmaven.javadoc.skip=true clean install -DcloneUi=true;
fi