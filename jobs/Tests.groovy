import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.trim

def jobs = []

jobs << job('accessibility-tests') {
    displayName('Accessibility Tests')
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

jobs << job('layout-tests') {
    displayName('Layout Tests')
    parameters {
        stringParam('target_platform', 'web', 'Target Platform - web or pub - informational website or publishing platform')
        stringParam('browser', '', 'Browser to test - firefox or chrome - blank is both')
        stringParam('groups', 'homepage, searchpage', 'Groups to run - homepage, searchpage, fundingpage, orglistpage')
    }
    scm {
        git(repo('beta-layout-tests'))
    }
    steps {
        shell(trim('''\
            set -e
            if [ -d "reports" ]; then
               echo "removing old reports";
               rm -fR reports/*;
            fi
            ./run.sh -t ${target_platform} -b ${browser} -g ${groups}
        '''))
    }

    publishers {
        archiveJunit('**/*.xml')
        // After upgrading to Job DSL 1.40, change above line to:
        // archiveTestNG('**/*.xml')
        publishHtml {
             report("reports/informationalWebsite/") {
                  reportName("Informational Website")
                  allowMissing(true)
             }
             report("reports/publishingPlatform/") {
                  reportName("Publishing Platform")
                  allowMissing(true)
             }
        }
    }

}

jobs << job('publishing-perf-tests') {
    displayName("Publishing Performance Tests")
    scm {
        git(repo('publishing-performance-tests'))
    }
    steps {
         maven('-B clean install sonar:sonar deploy')
    }
}

jobs << job('security-tests') {
    displayName('Security Tests')
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

listView('Test') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        jobs.each {
            name(it.name)
        }
    }
    columns {
        status()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}
