import static build.Utils.repo
import static build.Utils.trim

def jobs = []

jobs << job('accessibility-tests') {
    displayName('Accessibility Tests')
    parameters {
        choiceParam('environment', ['dev', 'int', 'exp', 'per', 'uat', 'tst', 'dgv', 'igv', 'egv', 'pgv', 'ugv', 'tgv'], 'Envirnoment to run the tests')
        choiceParam('website', ['mygov','gov'], 'Target Website to Validate')
        choiceParam('standard', ['WCAG2AA', 'Section508', 'WCAG2A', 'WCAG2AAA'], 'Accessibility Standard')
        choiceParam('keyword', ['error', 'warning', 'notice'], 'Keyword to signify errors')
    }
    logRotator {
        daysToKeep(60)
    }
    scm {
        git(repo('beta-website-accessibility-tests'), 'master')
    }
    steps {
        shell(trim('''\
            ./run.sh -e ${environment} -w ${website} -s ${standard} -k ${keyword}
        '''))
    }
    publishers {
        buildDescription('', '$website')
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
                  reportName("MyGov page with dropdown Report")
                  reportFiles("dropdown-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/mygov/html") {
                  reportName("MyGov Corporate Org Hub Report")
                  reportFiles("corp-org-hub-page.html")
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
             report("logs/gov/html") {
                  reportName("Gov Topic page Report")
                  reportFiles("topic-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Issue Hub page Report")
                  reportFiles("issue-hub-page.html")
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
        daysToKeep(60)
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
        choiceParam('testenv', ['int', 'dev', 'exp', 'per', 'uat', 'tst', 'dgv', 'igv', 'egv', 'ugv',' pgv'], 'Use this option to select test environment against which tests shall be executed')
        choiceParam('mode', ['single', 'multi'], 'Use this option to run the tests only in Chrome (single) or on Chrome, Firefox and Safari (multi)')
        choiceParam('smoke_only', ['false', 'true'], 'Use this option to ONLY run smoke tests')
        stringParam('selenium_ip_address', '10.21.134.66', 'Use this option to specify the IP address of the machine running Selenium web driver')
        stringParam('tests', 'all', 'Use this option to specify what tests to run. Enter a comma-separated (NO SPACES) list with any combination of these values: webE2E,pubE2E')
    }
    logRotator {
        daysToKeep(60)
    }
    scm {
        git(repo('beta-e2e'), 'master')
    }
    steps {
        shell(trim('''\
            if [ "\$smoke_only" = "true" ]; then
                ./run.sh -s ${site} -i ${selenium_ip_address} -m ${mode} -t ${tests} -e ${testenv} -k
            else
                ./run.sh -s ${site} -i ${selenium_ip_address} -m ${mode} -t ${tests} -e ${testenv}
            fi
        '''))
    }
    publishers {
        buildDescription('', '$site - $testenv')
        archiveJunit('reports/xml/*.xml')
    }
}

jobs << job('perceptual-testing') {
    displayName('Perceptual Tests')
    parameters {
        choiceParam('install_backstopJS', ['true', 'false'], 'Set to false to NOT install backstopJS')
        choiceParam('platform', ['www', 'pub'], 'Use this option to select tests for the site (www) or for Rubric (pub)')
        choiceParam('site', ['mygov', 'gov'], 'Use this option to select tests for mygov.scot or gov.scot')
        choiceParam('testenv', ['int', 'exp','per','blu','grn','igv','egv','ugv','pgv','bgv','ggv','live','local'], 'Use this option to select test environment to be compared against the reference env')
        choiceParam('referenceEnv', ['int', 'exp','per','blu','grn','igv','egv','ugv','pgv','bgv','ggv','live','local'], 'reference environment where base screenshots will be taken from')
    }
    logRotator {
        daysToKeep(60)
    }
    scm {
        git(repo('perceptual-testing'), 'master')
    }
    steps {
        shell(trim('''\
            if [ "\$install_backstopJS" = "true" ]; then
              ./run.sh -i -p ${platform} -s ${site} -r ${referenceEnv} -t ${testenv}
            else
              ./run.sh -p ${platform} -s ${site} -r ${referenceEnv} -t ${testenv}
            fi
        '''))
    }
    publishers {
        buildDescription('', '$site $platform - $testenv VS $referenceEnv')
        archiveJunit('backstop_data/**/ci_report/*.xml')
        publishHtml {
             report("backstop_data/www/mygov/html_report/big_res") {
                  reportName("MyGov website big resolutions Report")
                  reportFiles("index.html")
                  allowMissing()
                  keepAll()
             }
             report("backstop_data/www/mygov/html_report/small_res") {
                  reportName("MyGov website small resolutions Report")
                  reportFiles("index.html")
                  allowMissing()
                  keepAll()
             }
             report("backstop_data/www/gov/html_report/big_res") {
                  reportName("Gov website big resolutions Report")
                  reportFiles("index.html")
                  allowMissing()
                  keepAll()
             }
             report("backstop_data/www/gov/html_report/small_res") {
                  reportName("Gov website small resolutions Report")
                  reportFiles("index.html")
                  allowMissing()
                  keepAll()
             }
             report("backstop_data/pub/mygov/html_report") {
                  reportName("MyGov Rubric Report")
                  reportFiles("index.html")
                  allowMissing()
                  keepAll()
             }
             report("backstop_data/pub/gov/html_report") {
                  reportName("Gov Rubric Report")
                  reportFiles("index.html")
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
        choiceParam('test_env', ['int', 'dev', 'exp', 'per', 'uat', 'tst', 'dgv', 'igv', 'egv', 'pgv', 'ugv', 'tgv'], 'The test environment to be used')
        choiceParam('browser', ['all', 'chrome', 'firefox'], 'Browser to test')
        stringParam('webdriver_ip', '10.21.134.66', 'Use this option to specify the IP address of the machine running Selenium web driver')
        stringParam('groups', '', 'Leave empty for all. MyGov - articlepage, corporghubpage, doccollectionpage, fundingpage, guidepage, homepage, orglistpage, searchpage; Gov - apspage, cabinetandministerspage, civilservicepage, directoratepage, featuredrolepage, grouppage, homepage, issuehubpage, newspage, nonapspage, policypage, publicationspage, rolepage, searchpage, topicpage, topicspage')
    }
    logRotator {
        daysToKeep(60)
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
        buildDescription('', '$site $target_platform - $test_env')
        archiveTestNG('reports/**/xml/*.xml'){
          showFailedBuildsInTrendGraph()
          markBuildAsFailureOnFailedConfiguration()
        }
        publishHtml {
             report("reports/www/mygov/html") {
                  reportName("MyGov Site HTML Report")
                  reportFiles("index.html")
                  allowMissing()
                  keepAll()
             }
             report("reports/www/gov/html") {
                  reportName("Gov Site HTML Report")
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
        daysToKeep(60)
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
        daysToKeep(60)
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
