import static scot.mygov.jenkins.Utils.repo

job("Version Capture") {
    steps {
        shell(readFileFromWorkspace('resources/version-capture.sh'))
    }
}
