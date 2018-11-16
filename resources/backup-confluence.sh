#!/bin/sh
set -e
remotepath="/tmp/"
backupfilename=$(ssh -o StrictHostKeyChecking=no devops@confluence "ls -lart /tmp/*confluence-backup-inc-db.tgz | tail -1 | cut -d "/" -f3")

scp devops@confluence:"$remotepath""$backupfilename" .
aws s3 cp $backupfilename s3://scotgovdigitalbackups/confluence/confluence_latest.tgz

rm -fv $backupfilename
