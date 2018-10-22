#!/usr/bin/env sh

set -eu

OLD_VERSION=$1
NEW_VERSION=$2

perl -pi -e "s/$OLD_VERSION/$NEW_VERSION/g" .mvn/extensions.xml
perl -pi -e "s/$OLD_VERSION/$NEW_VERSION/g" lang/spec/.mvn/extensions.xml
perl -pi -e "s/\<spoofax\.version\>$OLD_VERSION\<\/spoofax\.version\>/<spoofax.version>$NEW_VERSION<\/spoofax.version>/g" pom/spoofax/pom.xml
