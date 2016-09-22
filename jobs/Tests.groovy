import static build.Utils.repo
import static build.Utils.trim

def jobs = []

jobs << job('accessibility-tests') {
    displayName('Accessibility Tests')
    parameters {
        choiceParam('website', ['Both', 'MyGov','Gov'], 'Target Website to Validate')
        choiceParam('standard', ['WCAG2AA', 'Section508', 'WCAG2A', 'WCAG2AAA'], 'Accessibility Standard')
        choiceParam('keyword', ['error', 'warning', 'notice'], 'Keyword to signify errors')
    }
    logRotator {
        daysToKeep(90)
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
             report("logs/mygov/html") {
                  reportName("MyGov page with dropdown Report")
                  reportFiles("dropdown-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }

             report("logs/gov/html") {
                  reportName("Gov Home page Report")
                  reportFiles("home-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov News landing page Report")
                  reportFiles("news-landing-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov News Item page Report")
                  reportFiles("news-item.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Policies landing page Report")
                  reportFiles("policies-landing-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Publications landing page Report")
                  reportFiles("publications-landing-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Non-APS Publication page Report")
                  reportFiles("publication-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov APS Publication page Report")
                  reportFiles("aps-publication-page.html")
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
                  reportName("Gov Cabinet and Ministers page Report")
                  reportFiles("cabinet-and-ministers.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Civil Service page Report")
                  reportFiles("civil-service.html")
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
                  reportName("Gov Featured Role page Report")
                  reportFiles("featured-role-page.html")
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

jobs << job('rubric-api-tests') {
    displayName('Rubric API tests')
    parameters {
        choiceParam('TESTENV', ['int', 'exp','per','tst','igv','egv','ugv','pgv','uat'], 'Use this option to select test environment against which tests shall be executed')
        choiceParam('groups', ['govsmoke', 'mygovsmoke','govregression','mygovregression'], 'Target Website to Validate')
    }
    logRotator {
        daysToKeep(90)
    }
    scm {
        git(repo('rubric-api-tests'), 'master')
    }
    steps {
        shell(trim('''\
            ./run.sh -e ${TESTENV} -g ${groups}
        '''))
    }
    publishers {
        archiveJunit('target/surefire-reports/junitreports/*.xml')
        publishHtml {
             report("target/surefire-reports") {
                  reportName("Rubric API Test Report")
                  reportFiles("index.html")
                  allowMissing()
                  keepAll()
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
    logRotator {
        daysToKeep(90)
    }
    scm {
        git(repo('beta-e2e'), 'master')
    }
    steps {
        shell(trim('''\
            ./run.sh -s ${site} -i ${selenium_ip_address} -m ${mode} -t ${tests} -e ${testenv}
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
    logRotator {
        daysToKeep(90)
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
    logRotator {
        daysToKeep(90)
    }
    scm {
        git(repo('publishing-performance-tests'))
    }
    steps {
         maven('-B clean install sonar:sonar deploy')
    }
}

jobs << job('security-tests') {
    displayName('Security Tests')
     parameters {
        choiceParam('test_env', ['int', 'dev', 'exp', 'per', 'blu','grn','dgv', 'igv', 'egv'], 'The test environment to be used')

    }
    logRotator {
        daysToKeep(90)
    }
    scm {
        git(repo('beta-security-tests'))
    }
    steps {
        shell(trim('''\
            cd SSL-Automated-Security-Tests
            ./AutomatedSSLSecurityTests.sh -e ${test_env}
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
