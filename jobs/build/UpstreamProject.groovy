package build

import javaposse.jobdsl.dsl.helpers.step.StepContext
import com.samskivert.mustache.Mustache
import com.samskivert.mustache.Template

import static build.Utils.trim

class UpstreamProject extends MyGovProject {

    String clean

    String build

    def boolean clean() {
        return !clean
    }

    def void build(def StepContext delegate) {
        def vars = [
            repo: repo,
            build: build,
            clean: clean,
            maven: maven,
            debian: debian
        ]

        if (maven) {
            def splits = maven.split(':')
            def groupId = splits[0]
            def artifactId = splits[1]
            vars << [
                groupId: groupId,
                artifactId: artifactId
            ]
        }
        def template = dsl.readFileFromWorkspace('resources/build-upstream')
        def compiled = Mustache.compiler().compile(template);
        def script = compiled.execute(vars)

        delegate.shell(script)
    }

}
