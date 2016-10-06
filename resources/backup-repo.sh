#!/bin/sh
ssh -o StrictHostKeyChecking=no devops@repo s3-nexus backup
ssh devops@repo s3-apt backup
ssh devops@repo s3-apt-cacher-ng backup
ssh devops@repo aws s3 sync --delete /srv/backups/ s3://mgs-repo/backups/
