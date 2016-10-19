set -ex
repo=deploy-pipeline
version=1.0.${BUILD_ID}
git clean -fdx
./build -v $version
git tag -a -m "Build ${version}" ${version}
git push --tags ssh://git@stash.digital.gov.uk:7999/mgv/${repo}.git "${version}"
mvn deploy:deploy-file \
  --batch-mode \
  -Dfile=pipeline_${version}_all.deb \
  -DgroupId=scot.mygov.pipeline \
  -DartifactId=pipeline \
  -Dversion="${version}" \
  -Dpackaging=deb \
  -DrepositoryId=release \
  -Durl=http://nexus/content/repositories/releases/
