package build

import javaposse.jobdsl.dsl.helpers.properties.PropertiesContext
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext
import javaposse.jobdsl.dsl.helpers.step.StepContext

import com.samskivert.mustache.Mustache
import com.samskivert.mustache.Template

import static build.Utils.slug
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
            version: version,
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

    def void publish(def PublisherContext delegate) {
        delegate.buildDescription(/^VERSION: ([0-9.-]*)/)
    }

    def String deployVersion(String debian, String env) {
        StringBuilder script = StringBuilder.newInstance()
        def slug = slug(name)
        script << "version=\$(curl -s http://localhost/job/${slug}/\${PROMOTED_ID}/api/xml?tree=description | xpath -q -e '//*/description/text()')\n"
        script << "pipeline deploy:${debian},\${version},${env} sync"
        return script.toString()
    }

    def String deploySshStep(String host, PrintStream out) {
        return ""
    }

}
