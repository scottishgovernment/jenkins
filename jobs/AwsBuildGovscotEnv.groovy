import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.trim

job("Aws Build Gov Scot Test Env") {
    parameters {
        choiceParam('env', ['igv', 'egv'], 'mygov.scot test environment')
    }
    scm {
        git('ssh://git@stash.digital.gov.uk:7999/mgv/aws.git')
    }
    steps {
        shell(trim('''\
            set -e
            tools/provisioning/vpc/aws_build_env_govscot.sh ${env}
            echo "INFO: Just built [BUILTENV] ${env}" \
        '''))
    }
    publishers {
        buildDescription('\\[BUILTENV\\] (\\w{3})')
    }
}
