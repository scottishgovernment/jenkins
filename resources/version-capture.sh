set -ex
version="1.0.${BUILD_ID}"
pipeline list:per | grep -E '[a-z-]+:\s[0-9\.]+' > versions.yaml
mvn deploy:deploy-file \
  --batch-mode \
  -Dfile=versions.yaml \
  -DgroupId=scot.mygov.release \
  -DartifactId=release \
  -Dversion="${version}" \
  -Dpackaging=yaml \
  -DrepositoryId=release \
  -Durl=http://repo.digital.gov.uk/nexus/content/repositories/releases/
