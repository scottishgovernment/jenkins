import static scot.mygov.jenkins.Utils.repo

job('puppet-apply') {
    displayName('Puppet Apply')
    parameters {
        choiceParam('env', ['int', 'exp', 'uat', 'per', 'grn', 'blu'], 'mygov.scot environment')
        choiceParam('dbrestore', ['false', 'true'], 'restore databases')
    }
    scm {
        git(repo('aws'))
    }
    steps {
        shell(readFileFromWorkspace('resources/puppet-apply.sh'))
    }
}
