import static scot.mygov.jenkins.Utils.repo

job('puppet-apply-mygov') {
    displayName('Puppet Apply MyGov')
    parameters {
        choiceParam(
            'env',
            ['dev', 'int', 'exp', 'uat', 'per', 'grn', 'blu'],
            'mygov.scot environment')
        choiceParam('dbrestore', ['false', 'true'], 'restore databases')
    }
    scm {
        git(repo('aws'))
    }
    steps {
        shell(readFileFromWorkspace('resources/puppet-apply.sh'))
    }
}
