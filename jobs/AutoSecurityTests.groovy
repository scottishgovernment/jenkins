import static scot.mygov.jenkins.Utils.repo

job("Auto Security Tests") {
    steps {
        shell(readFileFromWorkspace('resources/auto-security-tests.sh'))
    }
}
