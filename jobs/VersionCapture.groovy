import static scot.mygov.jenkins.Utils.repo

job('version-capture') {
    displayName('Version Capture')
    steps {
        shell(readFileFromWorkspace('resources/version-capture.sh'))
    }
}
