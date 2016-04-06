set -ex
repo=www-infrastructure
version="1.0.${BUILD_ID}"

cd puppet

git tag -a -m "Build ${version}" ${version}
git push --tags ssh://git@stash.digital.gov.uk:7999/mgv/${repo}.git "${version}"

./build -v ${version}

mvn deploy:deploy-file \
  --batch-mode \
  -Dfile=puppetry_${version}_all.deb \
  -DgroupId=org.mygovscot.puppet \
  -DartifactId=puppetry \
  -Dversion="${version}" \
  -Dpackaging=deb \
  -DrepositoryId=release \
  -Durl=http://repo.digital.gov.uk/nexus/content/repositories/releases/
