set -ex
repo=%repo%
version="1.0.${BUILD_ID}"

git clean -fdx -e node_modules
git update-ref --no-deref HEAD HEAD

checksum=node_modules/.npm
if [ ! -e "$checksum" ] || ! sha1sum --status -c "$checksum" ; then
  mkdir -p node_modules
  npm prune &&
    npm install &&
    sha1sum package.json npm-shrinkwrap.json > "$checksum"
    shasum -a1 package.json | awk '{print $1}' > node_modules/package.json.sha1
fi

deps=$(jq -r '.dependencies | to_entries[] | select(.value == "latest") | .key' package.json)
if [ -n "$deps" ]; then
  for dep in $deps; do
    npm install $dep@latest
    cd node_modules/$dep
    npm prune --production
    cd -
  done
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
