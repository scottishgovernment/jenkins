set -ex
repo=performance_platform
version="1.0.${BUILD_ID}"
git clean -fdx
git update-ref --no-deref HEAD HEAD
mvn -B \
  versions:set \
  versions:use-latest-versions \
  versions:update-properties \
  -DnewVersion="${version}" \
  -Dincludes='org.mygovscot.*,scot.mygov.*' \
  -DgenerateBackupPoms=false \
  -Pdebian
mvn -B versions:update-properties \
  -Dincludes=mygovscot.utils.version \
  -DgenerateBackupPoms
git commit -am "Set version to ${version}"
git tag -a -m "Build ${version}" ${version}
git push --tags ssh://git@stash.digital.gov.uk:7999/mgv/${repo}.git "${version}"
mvn -B -Prelease deploy -Pdebian
./refresh_db $version
mvn -B verify sonar:sonar -Psonar,it || true
