#!/bin/sh
set -e
remotepath="/tmp/"
backupfilename=$(ssh devops@jira "ls -lart /tmp/*jira*.tgz | cut -d "/" -f3")

scp devops@jira:"$remotepath""$backupfilename" .
aws s3api put-object --bucket scotgovdigitalbackups --key jira/jira_latest.tgz --body "$backupfilename"

rm -fv "$backupfilename"
