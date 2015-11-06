package scot.mygov.jenkins

import javaposse.jobdsl.dsl.helpers.step.StepContext

import static scot.mygov.jenkins.Utils.repo

class NodeProject extends MyGovProject {

    def List<String> dependencies

    def boolean clean() {
        return false
    }

    def void build(def StepContext delegate) {
        def template = dsl.readFileFromWorkspace('resources/node.sh')

        def colon = maven.indexOf(':')
        def groupId = maven.substring(0, colon)
        def artifactId = maven.substring(colon + 1)
        def deps = dependencies?.inject(new StringBuilder()) { result, dep ->
            return result.append("npm install ${dep}@latest")
        }

        def subs = [
          'repo': repo,
          'groupId': groupId,
          'artifactId': artifactId,
          'debian': debian,
          'dependencies': deps ?: ''
        ]

        def job = template
        subs.each { k, v ->
            job = job.replaceAll('%' + k + '%', v)
        }

        delegate.shell(job)
    }

}
