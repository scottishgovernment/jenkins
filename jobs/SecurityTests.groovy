import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.trim

job("Security Tests") {
    scm {
        git(repo('beta-security-tests'))
    }
    steps {
        shell(trim('''\
            set -e
            cd SSL-Automated-Security-Tests
            ./AutomatedSSLSecurityTests.sh perwww.mygov.scot
        '''))
    }
}
