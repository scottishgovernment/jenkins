#!/bin/sh
set -e

version=$(aws ec2 describe-vpcs \
  --filters "Name=tag:Name,Values=${env}_vpc" \
  --query Vpcs[].Tags[?Key==\`Version\`].Value \
  --output text)
version=${version:-RELEASE}

content=http://nexus/service/local/artifact/maven/content
curl -sSfo aws.deb \
  "${content}?g=scot.mygov.infrastructure&a=aws&v=${version}&r=releases&p=deb"

dpkg -x aws.deb .
cd opt/aws

%teardown%
