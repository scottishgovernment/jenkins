job("puppet-apply") {
    parameters {
        choiceParam('env', ['int', 'exp', 'uat', 'per', 'grn', 'blu'], 'mygov.scot environment')
        choiceParam('dbrestore', ['false', 'true'], 'restore databases')
    }
    scm {
        git('ssh://git@stash.digital.gov.uk:7999/mgv/aws.git')
    }
    steps {
        shell(readFileFromWorkspace('resources/puppet-apply.sh'))
    }
}
