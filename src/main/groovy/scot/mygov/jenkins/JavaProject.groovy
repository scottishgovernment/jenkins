package scot.mygov.jenkins

import java.util.regex.*
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class JavaProject {

    String name

    String repo

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

    def repo(name) {
        return "ssh://git@stash.digital.gov.uk:7999/mgv/" + repo + ".git"
    }

    def String java(name) {
        def job = new StringBuilder()
        job << buildRelease.replaceAll("%repo%", repo);
        if (snapshot) {
            job << '\n' << buildSnapshot.replace("%projects%", snapshot)
        }
        return job
    }

    Job build(DslFactory dslFactory) {
        dslFactory.job(name) {
            scm {
                git {
                    remote {
                        name('origin')
                        url(repo(name))
                    }
                    branch('refs/heads/master')
                }
            }
            steps {
                shell(java(name))
            }
        }
    }

    def trim(str) {
        def leadingWhitespace = Pattern.compile("\\s*")
        def matcher = leadingWhitespace.matcher(str)
        if (matcher.lookingAt()) {
            return str.replaceAll(Pattern.quote(matcher.group()), "")
        }
        return str;
    }

}
