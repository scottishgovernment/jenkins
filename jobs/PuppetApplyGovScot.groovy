import static scot.mygov.jenkins.Utils.repo

job('puppet-apply-gov') {
    displayName('Puppet Apply GovScot')
    parameters {
        choiceParam(
            'env',
            ['dgv', 'igv', 'egv', 'ugv', 'pgv'],
            'gov.scot environment')
        choiceParam('dbrestore', ['false', 'true'], 'restore databases')
        choiceParam('redisrestore', ['false', 'true'], 'restore redis and images')
    }
    scm {
        git(repo('aws'))
    }
    steps {
        shell(readFileFromWorkspace('resources/puppet-apply.sh'))
    }
}
