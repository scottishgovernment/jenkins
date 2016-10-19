package build

import javaposse.jobdsl.dsl.helpers.step.StepContext
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext
import com.samskivert.mustache.Mustache
import com.samskivert.mustache.Template

import static build.Utils.trim

class JavaProject extends MyGovProject {

    /**
     * Comma-separated list of modules to be built and published as snapshots.
     */
    String snapshot

    def void build(def StepContext delegate) {
      def template = dsl.readFileFromWorkspace('resources/build-java')
      def compiled = Mustache.compiler().compile(template);

      def script = compiled.execute([
        repo: repo,
        snapshot: snapshot
      ])

      delegate.shell(script)
    }

    def void publish(def PublisherContext delegate) {
        delegate.archiveJunit('**/target/surefire-reports/*.xml')
        delegate.postBuildScripts {
            steps {
                shell('sonar-check')
            }
            onlyIfBuildSucceeds()
            markBuildUnstable()
        }
    }

}
