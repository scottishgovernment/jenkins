package scot.mygov.jenkins

import java.util.regex.*
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class JavaProject {

    String name

    String repo

    def leadingWhitespace = Pattern.compile("^ *", Pattern.MULTILINE)

    def javaTemplate='''\
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

      #git checkout HEAD^
      #mvn -B deploy -DskipTests
    '''

    def repo(name) { return "ssh://git@stash.digital.gov.uk:7999/mgv/" + repo + ".git" }

    def strip(str) { return leadingWhitespace.matcher(str).replaceAll(""); }

    def java(name) {
      return "set -ex\nrepo=${repo}\n" + strip(javaTemplate);
    }


    Job build(DslFactory dslFactory) {
        dslFactory.job(name) {
          scm {
            git(repo(name))
          }
          steps {
            shell(java(name))
          }
        }
    }

}
