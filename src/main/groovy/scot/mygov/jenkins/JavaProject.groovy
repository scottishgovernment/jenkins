package scot.mygov.jenkins

import java.util.regex.*
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.helpers.properties.PropertiesContext

class JavaProject {

    String name

    String repo

    String snapshot

    String host

    String envs

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


    def deploy(PropertiesContext properties, def out) {
        def envs = this.envs == 'both' ? [ 'gov', 'mygov' ] : [ this.envs ]
        def prefixes = [ 'mygov': 'dev', 'gov': 'dgv' ]
        def hosts = envs
                .collect { prefixes.get(it) }
                .collectEntries {[ it, it + host ]}
        properties.promotions {
            hosts.each { k, v ->
                promotion {
                    name(k)
                    icon('star-gold')
                    conditions {
                        selfPromotion()
                    }
                    actions {
                        shell("echo ${v};")
                    }
                }
            }
        }
    }

    Job build(DslFactory dslFactory, out) {
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
            properties {
                if (host) {
                    deploy(delegate, out)
                }
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
