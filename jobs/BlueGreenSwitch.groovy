import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.trim

job('blue-green-switch') {
    displayName('Blue-Green Switch')
    parameters {
        choiceParam('env', ['blu', 'grn'], 'mygov.scot production environment')
    }
    scm {
        git(repo('aws'))
    }
    steps {
        shell(trim('''\
            cd tools/management/
            ./aws_blugrn_switch.sh ${env}
            echo "INFO: Just switched to [PRDENV] ${env}"\
        '''))
    }
    publishers {
        buildDescription('\\[PRDENV\\] (blu|grn)')
    }
}
