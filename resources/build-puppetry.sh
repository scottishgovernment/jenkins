set -ex
version="1.0.${BUILD_ID}"

cd puppet
if ! grep -c "$(curl -s jsonip.com  | jq -r ".ip")" modules/nginx/templates/site.erb >/dev/null ; then
  echo "Devnet IP address in site.erb is incorrect."
  exit 1
fi
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

