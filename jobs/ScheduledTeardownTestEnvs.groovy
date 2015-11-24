import static scot.mygov.jenkins.Utils.trim

buildFlowJob('scheduled-teardown-test-envs') {
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
