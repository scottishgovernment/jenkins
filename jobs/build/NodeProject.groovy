package build

import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext
import javaposse.jobdsl.dsl.helpers.step.StepContext
import com.samskivert.mustache.Mustache
import com.samskivert.mustache.Template

import static build.Utils.repo

class NodeProject extends MyGovProject {

    boolean publish

    boolean sonar = true

    def boolean clean() {
        return false
    }

    def void build(def StepContext delegate) {
        def template = dsl.readFileFromWorkspace('resources/build-node')
        def compiled = Mustache.compiler().compile(template);

        def vars = [
          repo: repo,
          debian: debian,
          publish: publish
        ]
        if (maven) {
            def colon = maven.indexOf(':')
            def groupId = maven.substring(0, colon)
            def artifactId = maven.substring(colon + 1)
            vars << [
                groupId: groupId,
                artifactId: artifactId
            ]
        }
        def script = compiled.execute(vars)

        delegate.shell(script)
        if (sonar) {
            delegate.shell {
                command('sonar-check')
                unstableReturn(1)
            }
        }
    }

}
