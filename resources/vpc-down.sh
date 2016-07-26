#!/bin/sh
set -e

version=$(pipeline list:${env} | awk '/aws:/{print $2}')
version=${version:-RELEASE}

content=http://nexus/service/local/artifact/maven/content
curl -sSfo aws.deb \
  "${content}?g=scot.mygov.infrastructure&a=aws&v=${version}&r=releases&p=deb"

dpkg -x aws.deb .
cd opt/aws

%teardown%
