import static scot.mygov.jenkins.Utils.repo

job("Layout Tests") {
    scm {
        git(repo('beta-layout-tests'))
    }
    steps {
        shell(readFileFromWorkspace('resources/layout-tests.sh'))
    }

    publishers {
        archiveJunit('**/*.xml')
        // After upgrading to Job DSL 1.40, change above line to:
        // archiveTestNG('**/*.xml')
    }

}
