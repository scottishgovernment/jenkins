import static scot.mygov.jenkins.Utils.repo

job("Pipeline Tools") {
    scm {
        git(repo('deploy-pipeline'))
    }
    steps {
        shell(readFileFromWorkspace('resources/pipeline-tools-build.sh'))
    }
}
