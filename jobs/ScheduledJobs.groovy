import static scot.mygov.jenkins.Utils.trim
import static scot.mygov.jenkins.Utils.awsRepo

def jobs = []

// define vars for conditional triggering based on environment
def env = System.getenv()
def myenv = env['FACTER_machine_env']
def enabled = myenv == "dev" || myenv == "services"



jobs << buildFlowJob('scheduled-rebuild-test-envs') {
    displayName('Scheduled Rebuild Test Environments')
    if (enabled) {
        triggers {
           cron('30 07 * * 1-5')
        }
    }
    buildFlow(trim('''
        build("gov-test-up", env: "igv")\n
        build("mygov-test-up", env: "int")\n
        build("mygov-test-up", env: "exp")\n
        build("mygov-full-up", env: "per")\n
        build("gov-test-up", env: "ugv")\n
        build("gov-test-up", env: "egv")
    '''))
}

jobs << buildFlowJob('scheduled-teardown-test-envs') {
    displayName('Scheduled Teardown Test Environments')
    if (enabled) {
        triggers {
           cron('30 19 * * 1-5')
      }
    }
    buildFlow(trim('''
        build("gov-test-down", env: "igv")\n
        build("mygov-test-down", env: "int")\n
        build("mygov-test-down", env: "exp")\n
        build("mygov-full-down", env: "per")\n
        build("gov-test-down", env: "ugv")\n
        build("gov-test-down", env: "egv")
    '''))
}


jobs << job('backup-production-s3-buckets') {
    displayName('Backup Production S3 Buckets')
    if (enabled) {
        triggers {
           cron('0 * * * 1-5')
        }
    }
    scm {
        awsRepo(delegate)
    }
    steps {
        shell(trim('''\
            cd tools/management/
            ./s3_restore mygov.scot backup
            ./s3_restore gov.scot backup
        '''))
    }
}

jobs << job('backup-jira') {
    displayName('Backup JIRA')
    if (enabled) {
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
    if (enabled) {
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
    if (enabled) {
        triggers {
            cron('00 04 * * 1-5')
        }
    }
    displayName('Backup Bitbucket')
    steps {
        shell(trim('''\
        #!/bin/bash

        set -e

        ssh devops@stash "sudo su - stash -c 'cd bitbucket-backup-client-3.2.0 && java -noverify -jar bitbucket-backup-client.jar'"
        scp devops@stash://home/stash/stash-backup-home/backups/*.tar .

        /usr/local/bin/aws s3api put-object --bucket scotgovdigitalbackups --key bitbucket/bitbucket_latest.tar --body *.tar

        rm -rf *.tar
        ssh devops@stash "sudo su - stash -c 'rm -rf /home/stash/stash-backup-home/backups/*.tar'"
        '''))
    }
}

jobs << job('backup-sonar') {
    if (enabled) {
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

jobs << job('Backup Repo') {
    if (enabled) {
        triggers {
            cron('H 4 * * 1-5')
        }
    }
    displayName('Backup Repo')
    steps {
        shell(trim('''\
        #!/bin/bash
        set -e
        ssh devops@repo aws s3 sync /media/application s3://reposerver-backup
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
