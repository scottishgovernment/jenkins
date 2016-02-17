import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.trim

def jobs = []

jobs << job('accessibility-tests') {
    displayName('Accessibility Tests')
    parameters {
        choiceParam('testenv', ['int', 'exp','per','tst','blu','grn','igv','egv','ugv','pgv','local'], 'Use this option to select test environment against which tests shall be executed')
        choiceParam('website', ['Both', 'MyGov','Gov'], 'Target Website to Validate')
        choiceParam('standard', ['WCAG2AA', 'Section508', 'WCAG2A', 'WCAG2AAA'], 'Accessibility Standard')
        choiceParam('keyword', ['error', 'warning', 'notice'], 'Keyword to signify errors')
    }
    scm {
        git(repo('beta-website-accessibility-tests'), 'master')
    }
    steps {
        shell(trim('''\
            ./run.sh -w ${website} -s ${standard} -k ${keyword}
        '''))
    }
    publishers {
        archiveJunit('logs/**/xml/*.xml')
        publishHtml {
             report("logs/mygov/html") {
                  reportName("MyGov Homepage Report")
                  reportFiles("home-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/mygov/html") {
                  reportName("MyGov Contact us Report")
                  reportFiles("contact-us-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/mygov/html") {
                  reportName("MyGov Search page Report")
                  reportFiles("search-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/mygov/html") {
                  reportName("MyGov Funding opportunities page Report")
                  reportFiles("funding-opportunities-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/mygov/html") {
                  reportName("MyGov Funding opportunities list Report")
                  reportFiles("funding-opportunities-list.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/mygov/html") {
                  reportName("MyGov Guide page Report")
                  reportFiles("guide-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/mygov/html") {
                  reportName("MyGov Organisations page Report")
                  reportFiles("organisations-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/mygov/html") {
                  reportName("MyGov Signpost page Report")
                  reportFiles("signpost-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }

             report("logs/gov/html") {
                  reportName("Gov Homepage Report")
                  reportFiles("home-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov News page Report")
                  reportFiles("news-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Policies page Report")
                  reportFiles("policies-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Directorates list Report")
                  reportFiles("directorates-atozlist.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Directorate page Report")
                  reportFiles("directorate-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Organisations page Report")
                  reportFiles("organisations-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Cabinet Ministers page Report")
                  reportFiles("organisations-cabinetministers.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Person page Report")
                  reportFiles("person-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Roles page Report")
                  reportFiles("roles-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Role page Report")
                  reportFiles("role-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
        }
    }
}

jobs << job('end-to-end-tests') {
    displayName('End-to-end tests')
    parameters {
        choiceParam('site', ['mygov', 'gov'], 'Use this option to select tests for mygov.scot or gov.scot')
        choiceParam('testenv', ['int', 'exp','per','blu','grn','igv','egv','ugv','pgv','local'], 'Use this option to select test environment against which tests shall be executed')
        choiceParam('mode', ['single', 'multi'], 'Use this option to run the tests only in Chrome (single) or on Chrome, Firefox and Safari (multi)')
        stringParam('selenium_ip_address', '10.21.134.83', 'Use this option to specify the IP address of the machine running Selenium web driver')
        stringParam('tests', 'all', 'Use this option to specify what tests to run. Enter a comma-separated (NO SPACES) list with any combination of these values: webE2E,pubE2E,webSmokeTests,pubSmokeTests,stagingSite')
    }
    scm {
        git(repo('beta-e2e'), 'master')
    }
    steps {
        shell(trim('''\
            ./run.sh -i ${selenium_ip_address} -m ${mode} -t ${tests} -e ${testenv}
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
        stringParam('webdriver_ip', '10.21.134.83', 'Use this option to specify the IP address of the machine running Selenium web driver')
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
