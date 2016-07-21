#!/bin/sh
set -e

curl -fgsS 'http://localhost/api/json?tree=jobs[name,nextBuildNumber]' | \
  jq -S ".jobs |
    map({key: .name, value: .nextBuildNumber}) |
    map(select(.key != \"$JOB_NAME\")) |
    from_entries" \
  > versions.json

if [ ! -r last.json ] || ! cmp -s versions.json last.json; then
  echo "Backing up job build numbers"
  aws s3 cp versions.json s3://mgs-jenkins/versions.json
  cp versions.json last.json
else
  echo "Build numbers have not changed. Not backing up."
fi
