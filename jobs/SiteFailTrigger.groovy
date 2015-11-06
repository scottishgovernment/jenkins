import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.trim

job('site-fail-trigger') {
    displayName('MyGov Site Fail Trigger')
    scm {
        git(repo('aws'))
    }
    steps {
        shell(trim('''\
            set -e
            cd tools/management/
            ./aws_sitefail_trigger.sh \
        '''))
    }
}
