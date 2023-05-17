import org.yaml.snakeyaml.Yaml
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext
import javaposse.jobdsl.dsl.jobs.WorkflowJob

import static build.Utils.trim
import static build.Utils.awsRepo

def jobs = []
def yaml = new Yaml().load(readFileFromWorkspace("resources/environments.yaml"))
def sites = yaml.sites

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
def myenv = env['ENVIRONMENT']
def enabled = myenv == "services"

def pipelineTriggerCron(WorkflowJob delegate, String value) {
    delegate.properties {
        pipelineTriggers {
            triggers {
                cron {
                    spec(value)
                }
            }
        }
    }
}

jobs << pipelineJob('scheduled-build-dev-envs') {
    displayName('Scheduled Build Dev Environments')
    logRotator {
        daysToKeep(90)
    }
    if (enabled) {
        pipelineTriggerCron(delegate, '15 07 * * 1-5')
    }
    definition {
      cps {
        def pipeline = StringBuilder.newInstance()
        pipeline << '''
            stage('Build') {
                def envs = ['dgv': 'gov', 'dev': 'mygov']
                def tasks = envs.collectEntries { name, site ->
                    job = {
                        catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                            build job: "${site}-${name}", parameters: [
                                string(name: 'action', value: 'build')
                            ]
                        }
                    }
                    [name, job]
                }
                parallel tasks
            }
        '''.stripIndent()
        script(pipeline.toString())
        sandbox()
      }
    }
}

jobs << pipelineJob('scheduled-teardown-dev-envs') {
    displayName('Scheduled Teardown Dev Environments')
    logRotator {
        daysToKeep(90)
    }
    if (enabled) {
        pipelineTriggerCron(delegate, '0 18 * * 1-5')
    }
    definition {
      cps {
        def pipeline = StringBuilder.newInstance()
        pipeline << '''
            def envs = ['dgv': 'gov', 'dev': 'mygov']
            def tasks = envs.collectEntries { name, site ->
                job = {
                    build job: "${site}-${name}", parameters: [
                        string(name: 'action', value: 'teardown')
                    ]
                }
                [name, job]
            }
            parallel tasks
        '''.stripIndent()
        script(pipeline.toString())
        sandbox()
      }
    }
}

jobs << pipelineJob('scheduled-teardown-exp-envs') {
    displayName('Scheduled Teardown Exp Environments')
    logRotator {
        daysToKeep(90)
    }
    if (enabled) {
        pipelineTriggerCron(delegate, '0 18 * * 1-5')
    }
    definition {
      cps {
        def pipeline = StringBuilder.newInstance()
        pipeline << '''
            def envs = ['egv':'gov', 'exp':'mygov']
            def tasks = envs.collectEntries { name, site ->
                job = {
                    build job: "${site}-${name}", parameters: [
                        string(name: 'action', value: 'teardown')
                    ]
                }
                [name, job]
            }
            parallel tasks
        '''.stripIndent()
        script(pipeline.toString())
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
           cron('0 * * * *')
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
    logRotator {
        daysToKeep(90)
    }
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
    displayName('Backup Confluence')
    logRotator {
        daysToKeep(90)
    }
    if (enabled) {
        triggers {
            cron('00 03 * * 1-5')
        }
    }
    steps {
        shell(readFileFromWorkspace('resources/backup-confluence.sh'))
    }
    publishers {
        slack(delegate)
    }

}

jobs << job('backup-git') {
    displayName('Backup Git')
    logRotator {
        daysToKeep(90)
    }
    if (enabled) {
        triggers {
            cron('00 21 * * 1-5')
        }
    }
    steps {
      shell('ssh git@git.digital.gov.uk backup offline')
    }
    publishers {
        slack(delegate)
    }
}

jobs << job('backup-sonar') {
    displayName('Backup SonarQube')
    logRotator {
        daysToKeep(90)
    }
    if (enabled) {
        triggers {
            cron('00 05 * * 1-5')
        }
    }
    steps {
        shell(readFileFromWorkspace('resources/backup-sonarqube'))
    }
    publishers {
        slack(delegate)
    }
}

jobs << job('macfs-backup') {
    displayName('MacFS Backup')
    logRotator {
        daysToKeep(90)
    }
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
    logRotator {
        daysToKeep(90)
    }
    if (enabled) {
        triggers {
            cron('H(0-29) 4 * * 1-5')
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

jobs << job('cleanup-builds') {
    displayName('Clean up old builds')
    logRotator {
        numToKeep(1)
    }
    if (enabled) {
        triggers {
            cron('H(50-59) 4 * * 1-5')
        }
    }
    steps {
        shell(trim('''\
            if [ -d ~/.m2/repository ]; then
              rm -rfv ~/.m2/repository/scot/gov/www
              rm -rfv ~/.m2/repository/scot/mygov/publishing
              find ~/.m2/repository -atime +90 -print -delete
            fi
        '''))
    }
    publishers {
        slack(delegate)
    }
}

jobs << pipelineJob('scheduled-run-integration') {
    displayName('Scheduled Integration Testing')
    logRotator {
        daysToKeep(90)
    }
    if (enabled) {
        pipelineTriggerCron(delegate, '00 07 * * 1-5')
    }
    definition {
      cps {
        def pipeline = StringBuilder.newInstance()
        pipeline << """
            stage('Integrate Test') {
                def envs = ['igv':'gov', 'int':'mygov']
                def tasks = envs.collectEntries { name, site ->
                    job = {
                        catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                            build job: 'integration-test-' + site
                        }
                    }
                    [site, job]
                }
                parallel tasks
            }
            
            stage('Build Exploratory Envs') {
                def envs = ['egv': 'gov', 'exp': 'mygov']
                def tasks = envs.collectEntries { name, site -> 
                    job = {
                        catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                            build job: 'build-' + name + '-environment'
                        }
                    }
                    [name, job]
                }
                parallel tasks
            }

        """.stripIndent()
        script(pipeline.toString())
        sandbox()
      }
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
