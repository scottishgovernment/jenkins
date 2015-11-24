import static scot.mygov.jenkins.Utils.trim

def jobs = []

jobs << buildFlowJob('scheduled-rebuild-test-envs') {
    displayName('Scheduled Rebuild Test Environments')
    triggers {
        cron('30 07 * * 1-5')
    }
    buildFlow(trim('''
        build("gov-test-up", env: "egv")\n
        build("gov-test-up", env: "igv")\n
        build("mygov-test-up", env: "int")\n
        build("mygov-test-up", env: "exp")
    '''))
}

jobs << buildFlowJob('scheduled-teardown-test-envs') {
    displayName('Scheduled Teardown Test Environments')
    triggers {
        cron('30 19 * * 1-5')
    }
    buildFlow(trim('''
        build("gov-test-down", env: "igv")\n
        build("gov-test-down", env: "egv")\n
        build("mygov-test-down", env: "int")\n
        build("mygov-test-down", env: "exp")
    '''))
}

listView('Scheduled Jobs') {
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
