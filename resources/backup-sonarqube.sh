#!/bin/bash
set -e
ssh -o StrictHostKeyChecking=no devops@sonar "sudo su - sonar -c 'sonarqube-db backup'"
scp devops@sonar:/opt/sonar/backups/sonarqube.ar .
aws s3 cp sonarqube.ar s3://scotgovdigitalbackups/sonar/sonarqube.ar
scp sonarqube.ar devops@repo:/srv/backups/services/sonarqube.ar
rm sonarqube.ar
