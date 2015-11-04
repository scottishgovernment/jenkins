package scot.mygov.jenkins

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.helpers.properties.PropertiesContext

import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.trim

class JavaProject {

    final String VERSION = '1.0.${PROMOTED_ID}'

    String name

    String repo

    String snapshot

    String host

    String site

    String debian

    String maven

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

    def deploySsh = trim('''\

    ''')

    Job build(DslFactory dslFactory, out) {
        try {
            return buildJob(dslFactory, out)
        } catch (Throwable t) {
            t.printStackTrace(out)
            throw t;
        }
    }

    Job buildJob(DslFactory dslFactory, PrintStream out) {
        return dslFactory.job(name) {
            scm {
                git {
                    remote {
                        name('origin')
                        url(repo(this.repo))
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

    def String java(name) {
        def job = new StringBuilder()
        job << buildRelease.replaceAll("%repo%", repo);
        if (snapshot) {
            job << '\n' << buildSnapshot.replace("%projects%", snapshot)
        }
        return job
    }

    def deploy(PropertiesContext properties, PrintStream out) {
        def sites = site == 'both' ? [ 'gov', 'mygov' ] : [ site ]
        def prefixes = [ 'mygov': 'dev', 'gov': 'dgv' ]
        def hosts = sites
                .collect { prefixes.get(it) }
                .collectEntries {[ it, it + host ]}
        def mygov = [ 'int', 'exp', 'uat', 'per', 'blu', 'grn']
        def gov = [ 'igv', 'egv']
        def siteHosts = []
        if (sites.contains('mygov')) {
            siteHosts << mygov
        }
        if (sites.contains('gov')) {
            siteHosts << gov
        }
        def envs = flatten(siteHosts)


        def i = 0;
        properties.promotions {

            hosts.each { env, host ->
                promotion {
                    name(sprintf("%02d", i++) + " " + env)
                    icon('star-gold')
                    conditions {
                        selfPromotion()
                    }
                    actions {
                        shell(deploySshStep(host, out))
                    }
                }
            }

            envs.each { nm ->
                promotion {
                    name(sprintf("%02d", i++) + " " + nm)
                    icon('star-gold')
                    conditions {
                        manual("")
                    }
                    actions {
                        shell("pipeline deploy:${debian},${VERSION},${nm}")
                    }
                }
            }

        }
    }

    def String deploySshStep(String host, PrintStream out) {
        def colon = maven.indexOf(':')
        def groupId = maven.substring(0, colon)
        def artifactId = maven.substring(colon + 1)

        def path = new StringBuilder()
        path << groupId.replace('.', '/') << '/'
        path << artifactId << '/'
        path << VERSION << '/'
        path << artifactId << '-' << VERSION << '.deb'

        def script = new StringBuilder("set -e\n")
        script << trim("""\
            name="${repo}"
            path="${path}"
            host="${host}"\n""")
        script << trim('''\
            repo=http://repo.digital.gov.uk/nexus/content/repositories/releases/
            curl -sSf "${repo}/${path}" | \\
              ssh -o StrictHostKeyChecking=no devops@${host} \\
                "cat - >/tmp/${name}.deb; sudo dpkg -i /tmp/${name}.deb"
        ''')
        return script
    }

    /**
     * Flattens a map of lists: [[ma, mb, mc], [ga, gb]] -> [ma, ga, mb, gb, mc]
     */
    def List<String> flatten(List<List<String>> lists) {
        def len = lists.collect { it.size() }.inject { a, b -> Math.max(a,b) }
        def result = []
        (0..len - 1).each { i ->
            (0..lists.size() - 1).each { j ->
                def list = lists.get(j)
                if (i < list.size()) {
                  result << list.get(i)
                }
            }
        }
        return result;
    }

}
