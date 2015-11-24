import static scot.mygov.jenkins.Utils.trim

buildFlowJob('scheduled-rebuild-test-envs') {
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
