import static scot.mygov.jenkins.Utils.repo

job("Layout Tests") {
    scm {
        git(repo('beta-layout-tests'))
    }
    steps {
        shell(readFileFromWorkspace('resources/layout-tests.sh'))
    }
/* **Below comment replaced with junit publisher until job-dsl plugin 1.40 release **
    publishers {
        archiveTestNG('**/*.xml')
    }
*/ 
    publishers {
        archiveJunit('**/*.xml')
    }
}
