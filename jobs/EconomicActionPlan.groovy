import static build.Utils.repo
import static build.Utils.trim

def jobs = []

jobs << job('economic-action-plan') {
    displayName('Economic Action Plan')

    logRotator {
        daysToKeep(60)
    }

    scm {
        git(repo('economicactionplan.mygov.scot'), 'master') {
            clean(true)
        }
    }

    triggers {
        scm('# Poll SCM enabled to allow trigger from git hook.')
    }

    steps {
        shell(trim('''\
            set -eux
            ./build
            if [ "$FACTER_machine_env" = "services" ]; then
              aws s3 sync --delete --acl public-read _site/ s3://economicactionplan.mygov.scot
            fi
        '''))
    }

    publishers {
        slackNotifier {
            notifyAborted(true)
            notifyFailure(true)
            notifyNotBuilt(true)
            notifyUnstable(true)
            notifyBackToNormal(true)
            notifyRepeatedFailure(true)
        }
    }

}
