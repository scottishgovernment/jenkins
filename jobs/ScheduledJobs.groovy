import static scot.mygov.jenkins.Utils.trim

def jobs = []

// define vars for conditional triggering based on environment
def env = System.getenv()
def myenv = env['FACTER_machine_env']

jobs << buildFlowJob('scheduled-rebuild-test-envs') {
    displayName('Scheduled Rebuild Test Environments')
    if (myenv == "dev") {
        triggers {
           cron('30 07 * * 1-5')
        }
    }
    buildFlow(trim('''
        build("gov-test-up", env: "igv")\n
        build("gov-test-up", env: "ugv")\n
        build("mygov-test-up", env: "int")\n
        build("mygov-test-up", env: "exp")\n
        build("mygov-full-up", env: "per")

    '''))
}

jobs << buildFlowJob('scheduled-teardown-test-envs') {
    displayName('Scheduled Teardown Test Environments')
    if (myenv == "dev") {
        triggers {
           cron('30 19 * * 1-5')
      }
    }
    buildFlow(trim('''
        build("gov-test-down", env: "igv")\n
        build("gov-test-down", env: "ugv")\n
        build("mygov-test-down", env: "int")\n
        build("mygov-test-down", env: "exp")\n
        build("mygov-full-down", env: "per")
    '''))
}

jobs << job('backup-jira') {
    displayName('Backup JIRA')
    if (myenv == "dev") {
        triggers {
            cron('00 03 * * 1-5')
        }
    }
    steps {
        shell(trim('''\
        #!/bin/bash
        set -e

        remotepath="/tmp/"
        backupfilename=$(ssh devops@jira "ls -lart /tmp/*jira*.tgz | cut -d "/" -f3")

        /usr/bin/scp devops@jira:"$remotepath""$backupfilename" .
        /usr/local/bin/aws s3api put-object --bucket scotgovdigitalbackups --key jira/jira_latest.tgz --body "$backupfilename"

        rm -fv "$backupfilename"
        '''))
    }
}

jobs << job('backup-confluence') {
    if (myenv == "dev") {
        triggers {
            cron('00 03 * * 1-5')
        }
    }
    displayName('Backup Confluence')
    steps {
        shell(trim('''\
        #!/bin/bash
        set -e

        remotepath="/tmp/"
        backupfilename=$(ssh devops@confluence "ls -lart /tmp/*confluence-backup-inc-db.tgz | tail -1 | cut -d "/" -f3")

        /usr/bin/scp devops@confluence:"$remotepath""$backupfilename" .
        /usr/local/bin/aws s3api put-object --bucket scotgovdigitalbackups --key confluence/confluence_latest.tgz --body "$backupfilename"

        rm -fv $backupfilename
        '''))
    }
}

jobs << job('backup-stash') {
    if (myenv == "dev") {
        triggers {
            cron('00 04 * * 1-5')
        }
    }
    displayName('Backup Stash')
    steps {
        shell(trim('''\
        #!/bin/bash

        set -e

        ssh devops@stash "cd /home/stash/stash-backup-client-1.3.1 && java -noverify -jar stash-backup-client.jar ; rm -rf /home/stash/stash-backup-home/backups/*.tar.gz && gzip /home/stash/stash-backup-home/backups/*.tar"
        scp devops@stash://home/stash/stash-backup-home/backups/*.tar.gz .

        /usr/local/bin/aws s3api put-object --bucket scotgovdigitalbackups --key stash/stash_latest.tar.gz --body *.tar.gz

        rm -rf *.tar.gz
        '''))
    }
}

jobs << job('backup-sonar') {
    if (myenv == "dev") {
        triggers {
            cron('00 05 * * 1-5')
        }
    }
    displayName('Backup SonarQube')
    steps {
        shell(trim('''\
        #!/bin/bash
        set -e
        ssh devops@sonar "sudo su - sonar -c 'sonarqube-db backup'"
        scp devops@sonar:/opt/sonar/backups/sonarqube.ar .
        aws s3 cp sonarqube.ar s3://scotgovdigitalbackups/sonar/sonarqube.ar
        scp sonarqube.ar devops@repo:DBbackups/services/sonarqube.ar
        rm sonarqube.ar
        '''))
    }
}

listView('Scheduled Jobs') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        jobs.each {
            name(it.name)
        }
    }
    columns {
        status()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}
