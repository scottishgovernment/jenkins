import static scot.mygov.jenkins.Utils.trim

def jobs = []

jobs << buildFlowJob('scheduled-rebuild-test-envs') {
    displayName('Scheduled Rebuild Test Environments')
    triggers {
        cron('30 07 * * 1-5')
    }
    buildFlow(trim('''
        build("gov-test-up", env: "egv")\n
        build("gov-test-up", env: "igv")\n
        build("mygov-test-up", env: "int")\n
        build("mygov-test-up", env: "exp")\n
        build("mygov-full-up", env: "per")
    '''))
}

jobs << buildFlowJob('scheduled-teardown-test-envs') {
    displayName('Scheduled Teardown Test Environments')
    triggers {
        cron('30 19 * * 1-5')
    }
    buildFlow(trim('''
        build("gov-test-down", env: "igv")\n
        build("gov-test-down", env: "egv")\n
        build("mygov-test-down", env: "int")\n
        build("mygov-test-down", env: "exp")\n
        build("mygov-full-down", env: "per")
    '''))
}

jobs << job('backup-jira') {
    displayName('Backup JIRA')
    triggers {
        cron('00 03 * * 1-5')
    }
    steps {
        shell(trim('''\
            set -e
            scp devops@jira:/tmp/*jira-backup-inc-db.tgz .
            /usr/local/bin/aws s3 cp ./ s3://scotgovdigitalbackups/jira/ \\
              --exclude "*" \\
              --include "*.tgz" \\
              --recursive \
        '''))
    }
}

jobs << job('backup-confluence') {
    triggers {
        cron('00 03 * * 1-5')
    }
    displayName('Backup Confluence')
    steps {
        shell(trim('''\
            set -e
            backupfilename=`ssh devops@confluence "ls -lart /home/confluence/confluence-home/backups/backup* |tail -1"`
            filename=`echo $backupfilename | awk '{ print $9 }'`
            scp devops@confluence:$filename .
            /usr/local/bin/aws s3 cp ./ s3://scotgovdigitalbackups/confluence/ \\
              --exclude "*" \\
              --include "*.zip" \\
              --recursive
        '''))
    }
}

jobs << job('backup-stash') {
    triggers {
        cron('00 04 * * 1-5')
    }
    displayName('Backup Stash')
    steps {
        shell(trim('''\
            set -e
            ssh devops@stash "cd /home/stash/stash-backup-client-1.3.1 && java -noverify -jar stash-backup-client.jar ; rm -rf /home/stash/stash-backup-home/backups/*.tar.gz && gzip /home/stash/stash-backup-home/backups/*.tar"
            scp devops@stash://home/stash/stash-backup-home/backups/*.tar.gz .
            /usr/local/bin/aws s3 cp ./ s3://scotgovdigitalbackups/stash/ \\
              --exclude "*" \\
              --include "*.tar.gz" \\
              --recursive 
            rm -rf *.tar.gz  
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
