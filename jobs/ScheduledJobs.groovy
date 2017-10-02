import static build.Utils.trim
import static build.Utils.awsRepo

import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext

def jobs = []

def void slack(def PublisherContext delegate) {
    delegate.slackNotifier {
        notifyAborted(true)
        notifyFailure(true)
        notifyNotBuilt(true)
        notifyUnstable(true)
        notifyBackToNormal(true)
        notifyRepeatedFailure(true)
    }
}


// define vars for conditional triggering based on environment
def env = System.getenv()
def myenv = env['FACTER_machine_env']
def enabled = myenv == "services"


jobs << pipelineJob('scheduled-build-test-envs') {
    displayName('Scheduled Build Test Environments')
    if (enabled) {
        triggers {
           cron('00 07 * * 1-5')
        }
    }
    definition {
      cps {
        script("""
          stage('Promote') {
            build job: 'promote-mygov', parameters: [
              string(name: 'from', value: 'dev'),
              string(name: 'to', value: 'int')
            ]
            build job: 'promote-gov', parameters: [
              string(name: 'from', value: 'dgv'),
              string(name: 'to', value: 'igv')
            ]
          }
          stage('Build') {
            build job: 'mygov-test-up', parameters: [string(name: 'env', value: 'int')]
            build job: 'gov-test-up',   parameters: [string(name: 'env', value: 'igv')]
            build job: 'mygov-test-up', parameters: [string(name: 'env', value: 'exp')]
            build job: 'mygov-test-up', parameters: [string(name: 'env', value: 'dev')]
            build job: 'gov-test-up',   parameters: [string(name: 'env', value: 'dgv')]
            build job: 'gov-test-up',   parameters: [string(name: 'env', value: 'egv')]
          }
        """.stripIndent())
        sandbox()
      }
    }
}


jobs << pipelineJob('scheduled-teardown-test-envs') {
    displayName('Scheduled Teardown Test Environments')
    if (enabled) {
        triggers {
           cron('30 19 * * 1-5')
        }
    }
    definition {
      cps {
        script("""
          def tasks = [:]

          tasks["dev"] = {
            build job: 'mygov-test-down', parameters: [string(name: 'env', value: 'dev')]
          }
          tasks["dgv"] = {
            build job: 'gov-test-down',   parameters: [string(name: 'env', value: 'dgv')]
          }
          tasks["exp"] = {
            build job: 'mygov-test-down', parameters: [string(name: 'env', value: 'exp')]
          }
          tasks["egv"] = {
            build job: 'gov-test-down',   parameters: [string(name: 'env', value: 'egv')]
          }
          tasks["int"] = {
            build job: 'mygov-test-down', parameters: [string(name: 'env', value: 'int')]
          }
          tasks["igv"] = {
            build job: 'gov-test-down',   parameters: [string(name: 'env', value: 'igv')]
          }
          parallel tasks
        """.stripIndent())
        sandbox()
      }
    }
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
