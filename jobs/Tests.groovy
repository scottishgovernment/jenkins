import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.trim

def jobs = []

jobs << job('accessibility-tests') {
    displayName('Accessibility Tests')
    parameters {
        choiceParam('standard', ['WCAG2AA', 'Section508', 'WCAG2A', 'WCAG2AAA'], 'Accessibility Standard')
        choiceParam('keyword', ['error', 'warning', 'notice'], 'Keyword to signify errors')
    }
    scm {
        git(repo('beta-website-accessibility-tests'), 'master')
    }
    steps {
        shell(trim('''\
            ./run.sh -s ${standard} -k ${keyword}
        '''))
    }
    publishers {
        publishHtml {
             report("logs/html") {
                  reportName("Homepage Report")
                  reportFiles("home-page.html")
                  keepAll()
             }
             report("logs/html") {
                  reportName("Contact us Report")
                  reportFiles("contact-us-page.html")
                  keepAll()
             }
             report("logs/html") {
                  reportName("Search page Report")
                  reportFiles("search-page.html")
                  keepAll()
             }
             report("logs/html") {
                  reportName("Funding opportunities page Report")
                  reportFiles("funding-opportunities-page.html")
                  keepAll()
             }
             report("logs/html") {
                  reportName("Funding opportunities list Report")
                  reportFiles("funding-opportunities-list.html")
                  keepAll()
             }
             report("logs/html") {
                  reportName("Guide page Report")
                  reportFiles("guide-page.html")
                  keepAll()
             }
             report("logs/html") {
                  reportName("Organisations page Report")
                  reportFiles("organisations-page.html")
                  keepAll()
             }
             report("logs/html") {
                  reportName("Signpost page Report")
                  reportFiles("signpost-page.html")
                  keepAll()
             }
        }
    }
}

jobs << job('end-to-end-tests') {
    displayName('End-to-end tests')
    parameters {
        choiceParam('mode', ['single', 'multi'], 'Use this option to run the tests only in Chrome (single) or on Chrome, Firefox and Safari (multi)')
        stringParam('selenium_ip_address', '10.21.134.45', 'Use this option to specify the IP address of the machine running Selenium web driver')
        stringParam('tests', 'all', 'Use this option to specify what tests to run. Enter a comma-separated (NO SPACES) list with any combination of these values: webE2E,pubE2E,webSmokeTests,pubSmokeTests,stagingSite')
    }
    scm {
        git(repo('beta-e2e'), 'master')
    }
    steps {
        shell(trim('''\
            ./run.sh -i ${selenium_ip_address} -m ${mode} -t ${tests}
        '''))
    }
    publishers {
        archiveJunit('reports/xml/*.xml')
        publishHtml {
             report("reports/html") {
                  reportName("Chrome Report")
                  reportFiles("CHROME_index.html")
                  allowMissing()
                  keepAll()
             }
             report("reports/html") {
                  reportName("Firefox Report")
                  reportFiles("FIREFOX_index.html")
                  allowMissing()
                  keepAll()
             }
             report("reports/html") {
                  reportName("Safari Report")
                  reportFiles("SAFARI_index.html")
                  allowMissing()
                  keepAll()
             }
        }
    }
}


jobs << job('layout-tests') {
    displayName('Layout Tests')
    parameters {
        choiceParam('site', ['mygov', 'gov'], 'Site to tests, either MyGov.scot or Gov.scot')
        choiceParam('target_platform', ['www', 'pub'], 'To run the tests either on Informational Website or Publishing Platform')
        choiceParam('test_env', ['int', 'dev', 'exp', 'per', 'dgv', 'igv', 'egv'], 'The test environment to be used')
        choiceParam('browser', ['all', 'chrome', 'firefox'], 'Browser to test')
        stringParam('webdriver_ip', '10.21.134.45', 'Use this option to specify the IP address of the machine running Selenium web driver')
        stringParam('groups', '', 'Groups to run - homepage, searchpage, fundingpage, orglistpage')
    }
    scm {
        git(repo('beta-layout-tests'), 'master') {
            clean(true)
        }
    }
    steps {
        shell(trim('''\
            ./run.sh -s ${site} -t ${target_platform} -e ${test_env} -b ${browser} ${groups:+-g ${groups}} ${webdriver_ip:+-i ${webdriver_ip}}
        '''))
    }

    publishers {
        archiveTestNG('reports/**/xml/*.xml'){
          showFailedBuildsInTrendGraph()
          markBuildAsFailureOnFailedConfiguration()
        }
        publishHtml {
             report("reports/www/mygov/html") {
                  reportName("MyGov Informational Website HTML Report")
                  reportFiles("index.html")
                  allowMissing()
                  keepAll()
             }
             report("reports/www/gov/html") {
                  reportName("Gov Informational Website HTML Report")
                  reportFiles("index.html")
                  allowMissing()
                  keepAll()
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
