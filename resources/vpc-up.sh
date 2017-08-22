#!/bin/sh
set -eu
ami=${ami_override:-%id%-$ami_NUMBER}
domain=%domain%

vpc_id() {
  aws ec2 describe-vpcs \
    --filters "Name=tag:Name,Values=${env}_vpc" \
    --query Vpcs[].VpcId \
    --output text
}

resolve() {
  mvn \
    org.apache.maven.plugins:maven-dependency-plugin:3.0.1:copy \
    -DoutputDirectory=. \
    -Dmdep.stripVersion \
    -Dartifact=${1} \
    >/dev/null
}

vpc=$(vpc_id)
if [ -n "$vpc" ]; then
  echo "VPC already exists for environment ${env}"
  exit 1
fi

ver=$(pipeline list:"${env}" | awk '/aws:/{print $2}')
ver=${ver:-RELEASE}
resolve scot.mygov.infrastructure:aws:${ver}:deb

version=$(dpkg --info aws.deb | awk '/Version/{print $2}')
echo "Environment: ${env}"
echo "Version:     ${version}"
echo "AMI:         ${ami}"
dpkg -x aws.deb .
cd opt/aws

tools/management/s3_restore "${domain}" "${env}"
exitcode=$(mktemp)
trap 'rm -f $exitcode' 0
(%build% 2>&1 || echo $? > "$exitcode") | ts %H:%M:%.S
if [ -f "$exitcode" ]; then
  ok=$(cat "$exitcode")
fi

vpc=$(vpc_id)
if [ -n "$vpc" ]; then
  aws ec2 create-tags --resources "${vpc}" --tags Key=Version,Value="${version}"
fi

exit $ok
