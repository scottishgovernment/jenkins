import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.trim

job("Aws Build Full Env") {
    parameters {
        choiceParam('env', ['per', 'blu', 'grn'], 'mygov.scot full scale environment')
    }
    scm {
        git('ssh://git@stash.digital.gov.uk:7999/mgv/aws.git')
    }
    steps {
        shell(trim('''\
            set -e
            echo tools/provisioning/vpc/aws_build_full_env.sh ${env}
            echo "INFO: Just built [BUILTENV] ${env}" \
        '''))
    }
    publishers {
        buildDescription('\\[BUILTENV\\] (\\w{3})')
    }
}
