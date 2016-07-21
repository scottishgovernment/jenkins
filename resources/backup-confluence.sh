#!/bin/sh
set -e
remotepath="/tmp/"
backupfilename=$(ssh devops@confluence "ls -lart /tmp/*confluence-backup-inc-db.tgz | tail -1 | cut -d "/" -f3")

scp devops@confluence:"$remotepath""$backupfilename" .
aws s3api put-object --bucket scotgovdigitalbackups --key confluence/confluence_latest.tgz --body "$backupfilename"

rm -fv $backupfilename
