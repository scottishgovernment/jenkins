job("blue-green-switch") {
    parameters {
        choiceParam('env', ['blu', 'grn'], 'mygov.scot production environment')
    }
    scm {
        git('ssh://git@stash.digital.gov.uk:7999/mgv/aws.git')
    }
    steps {
        shell('''\
cd tools/management/
echo ./aws_blugrn_switch.sh ${env}
echo "INFO: Just switched to [PRDENV] ${env}" \
            ''')
    }
    publishers {
        buildDescription('\\[PRDENV\\] (blu|grn)')
    }
}
