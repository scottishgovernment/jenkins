import static build.Utils.trim
import static build.Utils.awsRepo
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext
import javaposse.jobdsl.dsl.helpers.publisher.SlackNotificationsContext
def jobs = []

def void slack(def PublisherContext delegate) {
    delegate.slackNotifications {
        notifyAborted()
        notifyFailure()
        notifyNotBuilt()
        notifyUnstable()
        notifyBackToNormal()
    }
}


// define vars for conditional triggering based on environment
def env = System.getenv()
def myenv = env['FACTER_machine_env']
def enabled = myenv == "services"


jobs << buildFlowJob('scheduled-build-test-envs') {
    displayName('Scheduled Build Test Environments')
    if (enabled) {
        triggers {
           cron('00 07 * * 1-5')
        }
    }
    buildFlow(trim('''
        build("promote-gov", from: "dgv", to: "igv")\n
        build("promote-mygov", from: "dev", to: "int")\n
        build("mygov-test-up", env: "int")\n
        build("gov-test-up", env: "igv")\n
        build("mygov-test-up", env: "exp")\n
        build("mygov-test-up", env: "dev")\n
        build("gov-test-up", env: "dgv")\n
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
        build("mygov-test-down", env: "int")\n
        build("gov-test-down", env: "igv")\n
        build("mygov-test-down", env: "exp")\n
        build("gov-test-down", env: "dgv")\n
        build("mygov-test-down", env: "dev")\n
        build("gov-test-down", env: "egv")
    '''))
}


jobs << job('backup-production-s3-buckets') {
    displayName('Backup Production S3 Buckets')
    logRotator {
        daysToKeep(10)
    }
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
        shell(readFileFromWorkspace('resources/backup-jira.sh'))
    }
    publishers {
        slack(delegate)
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
        shell(readFileFromWorkspace('resources/backup-confluence.sh'))
    }
    publishers {
        slack(delegate)
    }

}

jobs << job('backup-stash') {
    displayName('Backup Bitbucket')
    if (enabled) {
        triggers {
            cron('00 04 * * 1-5')
        }
    }
    steps {
      shell(readFileFromWorkspace('resources/backup-bitbucket.sh'))
    }
    publishers {
        slack(delegate)
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
        shell(readFileFromWorkspace('resources/backup-sonarqube'))
    }
    publishers {
        slack(delegate)
    }
}

jobs << job('macfs-backup') {
    displayName('MacFS Backup')
    if (enabled) {
        triggers {
            cron('00 05 * * 1-5')
        }
    }
    steps {
        shell(trim('''\
        ssh -o StrictHostKeyChecking=no devops@macfs "sudo -iu macfsbackup sudo aws s3 sync --delete /opt/shared/ s3://macfs-backup/shared/"
        '''))
    }
    publishers {
        slack(delegate)
    }
}

jobs << job('backup-repo') {
    displayName('Backup Repo')
    if (enabled) {
        triggers {
            cron('H 4 * * 1-5')
        }
    }
    steps {
        shell(readFileFromWorkspace('resources/backup-repo.sh'))
    }
    publishers {
        slack(delegate)
    }
}

jobs << job('backup-jenkins') {
    displayName('Backup Jenkins')
    logRotator {
        numToKeep(1)
    }
    if (enabled) {
        triggers {
            cron('H/10 9-18 * * 1-5')
        }
    }
    steps {
        shell(readFileFromWorkspace('resources/backup-jenkins.sh'))
    }
    publishers {
        slack(delegate)
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
