#!/bin/sh
set -eu

resolve() {
  mvn \
    org.apache.maven.plugins:maven-dependency-plugin:3.0.1:copy \
    -DoutputDirectory=. \
    -Dmdep.overWriteReleases \
    -Dmdep.stripVersion \
    -Dartifact=${1} \
    >/dev/null
}

tag=$(aws ec2 describe-vpcs \
  --filters "Name=tag:Name,Values=${env}_vpc" \
  --query Vpcs[].Tags[?Key==\`Version\`].Value \
  --output text)

ver=${tag:-RELEASE}
resolve scot.mygov.infrastructure:aws:${ver}:deb

v=$(dpkg --info aws.deb | awk '/Version/{print $2}')
echo "Environment: ${env}"
echo "Tag:         ${tag:-none}"
echo "Version:     ${v}"

dpkg -x aws.deb .
cd opt/aws

exitcode=$(mktemp)
trap 'rm -f $exitcode' 0
(%teardown% 2>&1 || echo $? > "$exitcode") | ts %H:%M:%.S
if [ -f "$exitcode" ]; then
  ok=$(cat "$exitcode")
fi

exit $ok
