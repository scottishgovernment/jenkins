import static scot.mygov.jenkins.Utils.repo

job("Publishing Perf Tests") {
    scm {
        git(repo('publishing-performance-tests'))
    }
    steps {
         maven('-B clean install sonar:sonar deploy')
    }
}
