package scot.mygov.jenkins

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.helpers.step.StepContext
import javaposse.jobdsl.dsl.helpers.properties.PropertiesContext

import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.trim

class MyGovProject {

    final String VERSION = '1.0.${PROMOTED_ID}'

    DslFactory dsl

    PrintStream out

    String name

    String repo

    String host

    String site

    String debian

    String maven

    Job build(DslFactory dslFactory, out) {
        this.dsl = dslFactory
        this.out = out
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
                build(delegate)
            }
            properties {
                deploy(delegate, out)
            }
        }
    }

    def void build(def StepContext delegate) {

    }

    def deploy(PropertiesContext properties, PrintStream out) {
        def sites = site == 'both' ? [ 'gov', 'mygov' ] : [ site ]
        def devEnvs = [ 'dev', 'dgv' ]
        def envs = []
        if (debian) {
            def mygov = [ 'dev', 'int', 'exp', 'uat', 'per', 'blu', 'grn']
            def gov = [ 'dgv', 'igv', 'egv']
            def siteEnvs = []
            if (sites.contains('mygov')) {
                siteEnvs << mygov
            }
            if (sites.contains('gov')) {
                siteEnvs << gov
            }
            envs = flatten(siteEnvs)
        }

        def i = 0;
        properties.promotions {
            envs.each { nm ->
                def isDev = devEnvs.contains(nm)
                promotion {
                    name(sprintf("%02d", i++) + " " + nm)
                    icon('star-gold')
                    conditions {
                        if (isDev) {
                            selfPromotion()
                        } else {
                            manual("")
                        }
                    }
                    actions {
                        shell("pipeline deploy:${debian},${VERSION},${nm}")
                        if (isDev && host) {
                            shell(deploySshStep(host, out))
                        }
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
            curl -sSf -o "${name.deb}" "${repo}/${path}"
            scp "${name.deb}" "devops@${host}:/tmp/${name.deb}"
            ssh -o StrictHostKeyChecking=no devops@${host} "sudo dpkg -i /tmp/${name}.deb"
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
