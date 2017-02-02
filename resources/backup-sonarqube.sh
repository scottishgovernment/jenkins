#!/bin/sh
set -e
ssh -o StrictHostKeyChecking=no devops@sonar sonarqube-db backup
files="sonarqube.ar sonarqube.sql.gz"
for file in $files; do
  scp "devops@sonar:/opt/sonar/backups/${file}" .
  aws s3 cp "$file" "s3://scotgovdigitalbackups/sonar/${file}"
  scp "$file" "devops@repo:/srv/backups/services/${file}"
  rm "$file"
done
