import static scot.mygov.jenkins.Utils.repo

job("Layout Tests") {
    scm {
        git(repo('beta-layout-tests'))
    }
    steps {
        shell(readFileFromWorkspace('resources/layout-tests.sh'))
    }
    publishers {
        archiveTestNG('**/*.xml')
    }
}
