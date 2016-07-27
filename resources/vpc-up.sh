#!/bin/sh
set -e
ami=${override:-$version_NUMBER}
domain=%domain%

vpc_id() {
  aws ec2 describe-vpcs \
    --filters "Name=tag:Name,Values=${env}_vpc" \
    --query Vpcs[].VpcId \
    --output text
}

vpc=$(vpc_id)
if [ -n "$vpc" ]; then
  echo "VPC already exists for environment ${env}"
  exit 1
fi

version=$(pipeline list:${env} | awk '/aws:/{print $2}')
version=${version:-RELEASE}

content=http://nexus/service/local/artifact/maven/content
curl -sSfo aws.deb \
  "${content}?g=scot.mygov.infrastructure&a=aws&v=${version}&r=releases&p=deb"

v=$(dpkg --info aws.deb | awk '/Version/{print $2}')
echo "Environment: ${env}"
echo "Version:     ${v}"
echo "AMI:         ${version}"
dpkg -x aws.deb .
cd opt/aws

tools/management/s3_restore ${domain} ${env}
%build% || ok=$?

vpc=$(vpc_id)
if [ -n "$vpc" ] && [ "$version" != "RELEASE" ]; then
  aws ec2 create-tags --resources ${vpc} --tags Key=Version,Value=${version}
fi

exit $ok
