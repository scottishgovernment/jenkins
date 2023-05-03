#!/bin/sh
set -eu
remote=devops@bitbucket.digital.gov.uk

# Runs a command as the stash user
run() {
  cmd="$1"
  command ssh \
  -o StrictHostKeyChecking=accept-new \
  -o RequestTTY=no \
  "$remote" \
  "sudo su - stash -c '$cmd'"
}

run '/bin/sh -eu' <<EOF
cd bitbucket-backup-client-3.2.0
java -noverify -jar bitbucket-backup-client.jar
EOF

scp "${remote}://home/stash/stash-backup-home/backups/*.tar" .

aws s3api put-object --bucket scotgovdigitalbackups --key bitbucket/bitbucket_latest.tar --body *.tar

rm -rf *.tar
run 'rm -rf /home/stash/stash-backup-home/backups/*.tar'
