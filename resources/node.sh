set -ex
repo=%repo%
version="1.0.${BUILD_ID}"

git clean -fdx -e node_modules
git update-ref --no-deref HEAD HEAD

%dependencies%
./build --ci -v "${version}"

git tag -a -m "Build ${version}" ${version}
git push --tags ssh://git@stash.digital.gov.uk:7999/mgv/${repo}.git "${version}"

mvn deploy:deploy-file \
  --batch-mode \
  -Dfile=%debian%_${version}_all.deb \
  -DgroupId=%groupId% \
  -DartifactId=%artifactId% \
  -Dversion="${version}" \
  -Dpackaging=deb \
  -DrepositoryId=release \
  -Durl=http://repo.digital.gov.uk/nexus/content/repositories/releases/
