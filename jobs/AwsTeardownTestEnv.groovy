import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.trim

job("Aws Teardown Test Env") {
    parameters {
        choiceParam('env', ['int', 'exp', 'uat'], 'mygov.scot test environment')
    }
    scm {
        git('ssh://git@stash.digital.gov.uk:7999/mgv/aws.git')
    }
    steps {
        shell(trim('''\
            set -e
            echo tools/provisioning/vpc/aws_teardown_env.sh ${env}
            echo "INFO: Just tore down [BUILTENV] ${env}" \
        '''))
    }
    publishers {
        buildDescription('\\[BUILTENV\\] (\\w{3})')
    }
}
