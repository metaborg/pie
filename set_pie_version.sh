#!/usr/bin/env sh

set -eu

OLD_VERSION=$1
NEW_VERSION=$2

mvn -f pom/pom.xml versions:set -DnewVersion=$NEW_VERSION -DgenerateBackupPoms=false

perl -pi -e "s/$OLD_VERSION/$NEW_VERSION/g" lang/spec/metaborg.yaml
perl -pi -e "s/$OLD_VERSION/$NEW_VERSION/g" lang/example/metaborg.yaml
perl -pi -e "s/$OLD_VERSION/$NEW_VERSION/g" lang/test/metaborg.yaml
