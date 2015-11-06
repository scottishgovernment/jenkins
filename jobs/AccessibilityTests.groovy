import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.trim

job("Accessibility Tests") {
    parameters {
        stringParam('standard', 'WCAG2AA', 'Accessibility Standard')
        stringParam('keyword', 'error', 'Keyword to signify errors')
    }
    scm {
        git(repo('beta-website-accessibility-tests'))
    }
    steps {
        shell(trim('''\
            set -e
            ./run-tests.sh -s ${standard} -k ${keyword}
        '''))
    }
}
