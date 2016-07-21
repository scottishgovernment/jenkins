#!/bin/sh
set -e

ssh devops@stash "sudo su - stash -c 'cd bitbucket-backup-client-3.2.0 && java -noverify -jar bitbucket-backup-client.jar'"
scp devops@stash://home/stash/stash-backup-home/backups/*.tar .

aws s3api put-object --bucket scotgovdigitalbackups --key bitbucket/bitbucket_latest.tar --body *.tar

rm -rf *.tar
ssh devops@stash "sudo su - stash -c 'rm -rf /home/stash/stash-backup-home/backups/*.tar'"
