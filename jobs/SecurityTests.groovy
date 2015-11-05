import static scot.mygov.jenkins.Utils.repo

job("Security Tests") {
    steps {
        shell(readFileFromWorkspace('resources/security-tests.sh'))
    }
}
