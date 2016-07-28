#!/bin/sh
set -e
ami=${ami_override:-$ami_NUMBER}
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

content=http://nexus/service/local/artifact/maven/content
ver=$(pipeline list:"${env}" | awk '/aws:/{print $2}')
ver=${ver:-RELEASE}
curl -sSfo aws.deb \
  "${content}?g=scot.mygov.infrastructure&a=aws&v=${ver}&r=releases&p=deb"

version=$(dpkg --info aws.deb | awk '/Version/{print $2}')
echo "Environment: ${env}"
echo "Version:     ${version}"
echo "AMI:         ${ami}"
dpkg -x aws.deb .
cd opt/aws

tools/management/s3_restore "${domain}" "${env}"
%build% || ok=$?

vpc=$(vpc_id)
if [ -n "$vpc" ]; then
  aws ec2 create-tags --resources "${vpc}" --tags Key=Version,Value="${version}"
fi

exit $ok
