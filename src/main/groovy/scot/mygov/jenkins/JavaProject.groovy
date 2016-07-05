package scot.mygov.jenkins

import javaposse.jobdsl.dsl.helpers.step.StepContext
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext

import static scot.mygov.jenkins.Utils.trim

class JavaProject extends MyGovProject {

    /**
     * Comma-separated list of modules to be built and published as snapshots.
     */
    String snapshot

    def buildRelease=trim('''\
      set -ex
      repo=%repo%
      version="1.0.${BUILD_ID}"

      git clean -fdx
      git update-ref --no-deref HEAD HEAD
      mvn -B versions:set versions:use-latest-versions \\
        -DnewVersion="${version}" \\
        -Dincludes='org.mygovscot.*,scot.mygov.*' \\
        -DgenerateBackupPoms=false
      git commit -am "Set version to ${version}"
      git tag -a -m "Build ${version}" ${version}

      git push --tags ssh://git@stash.digital.gov.uk:7999/mgv/${repo}.git "${version}"
      mvn -B -Prelease deploy sonar:sonar
    ''')

    def buildSnapshot=trim('''\
        git checkout HEAD^
        mvn -B source:jar deploy -DskipTests %projects%
    ''')


    def void build(def StepContext delegate) {
        delegate.shell(java(name))
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

    def String java(name) {
        def job = new StringBuilder()
        job << buildRelease.replaceAll("%repo%", repo);
        if (snapshot != null) {
            def args = snapshot ? "-am -pl ${snapshot}" : ""
            job << '\n' << buildSnapshot.replace("%projects%", args)
        }
        return job
    }

}
