package build

import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext
import javaposse.jobdsl.dsl.helpers.step.StepContext
import com.samskivert.mustache.Mustache
import com.samskivert.mustache.Template

import static build.Utils.repo

class NodeProject extends MyGovProject {

    boolean publish

    def boolean clean() {
        return false
    }

    def void build(def StepContext delegate) {
        def template = dsl.readFileFromWorkspace('resources/build-node')
        def compiled = Mustache.compiler().compile(template);

        def colon = maven.indexOf(':')
        def groupId = maven.substring(0, colon)
        def artifactId = maven.substring(colon + 1)

        def script = compiled.execute([
            repo: repo,
            groupId: groupId,
            artifactId: artifactId,
            debian: debian,
            publish: publish
        ])

        delegate.shell(script)
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
