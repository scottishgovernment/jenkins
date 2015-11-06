import static scot.mygov.jenkins.Utils.trim

job('backup-jira') {
    displayName('Backup JIRA')
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

job('backup-confluence') {
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
