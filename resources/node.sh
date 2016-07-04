set -ex
repo=%repo%
version="1.0.${BUILD_ID}"

git clean -fdx -e node_modules
git update-ref --no-deref HEAD HEAD

dependencies="%dependencies%"
if [ -n "$dependencies" ]; then
  # Install dependencies not managed through npm-shrinkwrap.json
  tmp=$(mktemp -d /tmp/npm-XXXXXX)
  trap "rm -r \"$tmp\"" 0
  cd "$tmp"
  ln -s $OLDPWD lib
  globalconfig=$(npm config get globalconfig)
  globalignorefile=$(npm config get globalignorefile)
  args="--globalconfig=$globalconfig \
    --globalignorefile=$globalignorefile \
    --prefix=$PWD"
  for package in $dependencies; do
    npm install $args -g --production "$package@latest"
  done
  cd -
fi

jq .version=\"${version}\" package.json | sponge package.json
git add package.json
git commit -m "Set version to ${version}"
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
