from fabric.api import task, cd, lcd, local, env, sudo, run, settings, hide
from fabric.colors import green, blue, red, yellow

env.use_ssh_config = True
env.disable_known_hosts = True
env.user = 'devops'


@task
def servers():
    env.hosts = [
        'gabortest.digital.gov.uk',
        'gabortest2.digital.gov.uk'
    ]


@task
def pwd():
    run('hostname')
    run('pwd')


@task
def apply():
    sudo("""echo "deb [arch=amd64] http://apt.digital.gov.uk services main" > \
        /etc/apt/sources.list.d/services.list""")
    sudo('apt-get update')
    sudo('apt-get install puppetry')
    with cd('/tmp'):
        sudo("""if [ ! -l current_puppet ]; then
          ln -s consolidated_puppet current_puppet;
          fi""")
    with cd("/tmp/current_puppet"):
        run("./apply")
