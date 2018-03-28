set -ex
repo=repo-clean
version=1.0.${BUILD_ID}
git clean -fdx
./build --ci -v $version
git tag -a -m "Build ${version}" ${version}
git push --tags ssh://git@stash.digital.gov.uk:7999/mgv/${repo}.git "${version}"
mvn deploy:deploy-file \
  --batch-mode \
  -Dfile=repo-clean_${version}_amd64.deb \
  -DgroupId=scot.mygov.pipeline \
  -DartifactId=repo-clean \
  -Dversion="${version}" \
  -Dpackaging=deb \
  -DrepositoryId=release \
  -Durl=http://nexus/repository/releases/
