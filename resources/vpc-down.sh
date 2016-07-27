#!/bin/sh
set -e

tag=$(aws ec2 describe-vpcs \
  --filters "Name=tag:Name,Values=${env}_vpc" \
  --query Vpcs[].Tags[?Key==\`Version\`].Value \
  --output text)

content=http://nexus/service/local/artifact/maven/content
curl -sSfo aws.deb \
  "${content}?g=scot.mygov.infrastructure&a=aws&v=${tag:-RELEASE}&r=releases&p=deb"

v=$(dpkg --info aws.deb | awk '/Version/{print $2}')
echo "Environment: ${env}"
echo "Tag:         ${tag:-none}"
echo "Version:     ${v}"

dpkg -x aws.deb .
cd opt/aws

%teardown%
