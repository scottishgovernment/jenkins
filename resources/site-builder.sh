set -ex
repo=site-builder
version="1.0.${BUILD_ID}"

git clean -fdx -e node_modules
git update-ref --no-deref HEAD HEAD

jq .version=\"${version}\" package.json | sponge package.json
git add package.json
git commit -m "Set version to ${version}"
git tag -a -m "Build ${version}" ${version}
git push --tags ssh://git@stash.digital.gov.uk:7999/mgv/${repo}.git "${version}"

./build --ci -v "${version}"
npm publish

mvn deploy:deploy-file \
  --batch-mode \
  -Dfile=site-builder_${version}_all.deb \
  -DgroupId=scot.mygov.site \
  -DartifactId=site-builder \
  -Dversion="${version}" \
  -Dpackaging=deb \
  -DrepositoryId=release \
  -Durl=http://repo.digital.gov.uk/nexus/content/repositories/releases/
