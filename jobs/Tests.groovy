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
            ./run-tests.sh -s ${standard} -k ${keyword}
        '''))
    }
}

jobs << job('multi-browser-tests') {
    displayName('Multi-browser tests')
    scm {
        git(repo('beta-e2e'))
    }
    steps {
        shell(trim('''\
            npm prune
            npm install
            grunt install update
            grunt protractor:websiteSmokeTests \\
              --configFile=protractor.multibrowser.conf.js \\
              --website=https://intwww.mygov.scot

            grunt screenshots --website=https://intwww.mygov.scot
            mv screenshots screenshots.${BUILD_ID}
            tar zcf screenshots.tar.gz screenshots.${BUILD_ID}
            scp screenshots.tar.gz /var/tmp/screenshots/screenshots.tar.gz
            ssh devops@repo "cd /home/devops/screenshot_archive; \\
                tar zxf /var/tmp/screenshots.tar.gz; rm -f /var/tmp/screenshots.tar.gz"
        '''))
    }
    publishers {
        archiveJunit('**/test-results/junit/*.xml')
    }
}

jobs << job('end-to-end-tests') {
    displayName('End-to-end tests')
    scm {
        git(repo('beta-e2e'))
    }
    steps {
        shell(trim('''\
            npm prune
            npm install
            grunt install update
            grunt test:e2e
        '''))
    }
    publishers {
        archiveJunit('**/test-results/junit/*.xml')
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
        git(repo('beta-layout-tests')) {
            clean(true)
        }
    }
    steps {
        shell(trim('''\
            ./run.sh -t ${target_platform} ${browser:+-b ${browser}} -g ${groups}
        '''))
    }

    publishers {
        archiveJunit('**/*.xml')
        // After upgrading to Job DSL 1.40, change above line to:
        // archiveTestNG('**/*.xml')
        /*
        publishHtml {
             report("reports/informationalWebsite/") {
                  reportName("Informational Website")
                  allowMissing(true)
             }
             report("reports/publishingPlatform/") {
                  reportName("Publishing Platform")
                  allowMissing(true)
             }
        }*/
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
