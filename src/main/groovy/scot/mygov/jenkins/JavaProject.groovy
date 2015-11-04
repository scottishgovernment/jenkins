package scot.mygov.jenkins

import javaposse.jobdsl.dsl.helpers.step.StepContext

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

      mvn -B verify
      #git push --tags ssh://git@stash.digital.gov.uk:7999/mgv/${repo}.git "${version}"
      #mvn -B -Prelease verify sonar:sonar deploy
    ''')

    def buildSnapshot=trim('''\
        git checkout HEAD^
        mvn -B source:jar install -am -pl %projects% -DskipTests
        # mvn -B source:jar deploy -am -pl %projects% -DskipTests
    ''')


    def void build(def StepContext delegate) {
        delegate.shell(java(name))
    }

    def String java(name) {
        def job = new StringBuilder()
        job << buildRelease.replaceAll("%repo%", repo);
        if (snapshot) {
            job << '\n' << buildSnapshot.replace("%projects%", snapshot)
        }
        return job
    }

}
