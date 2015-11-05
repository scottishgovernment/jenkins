import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.trim

job("Backup JIRA") {
    steps {
        shell(trim('''\
            set -e
            scp devops@jira:/tmp/*jira-backup-inc-db.tgz .
            /usr/local/bin/aws s3 cp ./ s3://scotgovdigitalbackups/jira/ --exclude "*" --include "*.tgz" --recursive \
        '''))
    }
}
