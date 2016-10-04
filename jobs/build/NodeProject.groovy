package build

import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext
import javaposse.jobdsl.dsl.helpers.step.StepContext

import static build.Utils.repo

class NodeProject extends MyGovProject {

    boolean publish

    def boolean clean() {
        return false
    }

    def void build(def StepContext delegate) {
        def template = dsl.readFileFromWorkspace('resources/node.sh')

        def colon = maven.indexOf(':')
        def groupId = maven.substring(0, colon)
        def artifactId = maven.substring(colon + 1)

        def subs = [
          'repo': repo,
          'groupId': groupId,
          'artifactId': artifactId,
          'debian': debian,
        ]

        def job = template
        subs.each { k, v ->
            job = job.replaceAll('%' + k + '%', v)
        }

        delegate.shell(job)
        if (publish) {
            delegate.shell('npm shrinkwrap && npm publish')
        }
    }

    def void publish(def PublisherContext delegate) {
        delegate.postBuildScripts {
            steps {
                shell('sonar-check')
            }
            onlyIfBuildSucceeds()
            markBuildUnstable()
        }
    }

}
