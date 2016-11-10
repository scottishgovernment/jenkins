from fabric.api import task, cd, lcd, local, env, sudo, run, settings, hide
from fabric.colors import green, blue, red, yellow

env.use_ssh_config = True
env.disable_known_hosts = True
env.user = 'devops'

@task
def servers():
    env.hosts = ['gabortest.digital.gov.uk', 'gabortest2.digital.gov.uk']

@task
def package():
    sudo("curl -sSo /tmp/currentpuppet.deb 'http://repo.digital.gov.uk/nexus/service/local/artifact/maven/content?g=org.mygovscot.puppet&a=puppetry&v=RELEASE&r=releases&p=deb'")

@task
def install():
    sudo("dpkg -i /tmp/currentpuppet.deb")

@task
def apply():
    with cd("/tmp/current_puppet"):
     run("./apply")
