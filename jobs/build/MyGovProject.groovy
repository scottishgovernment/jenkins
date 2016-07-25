package build

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.helpers.step.StepContext
import javaposse.jobdsl.dsl.helpers.properties.PropertiesContext
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext
import javaposse.jobdsl.dsl.helpers.publisher.SlackNotificationsContext
import org.yaml.snakeyaml.Yaml

import static build.Utils.repo
import static build.Utils.slug
import static build.Utils.trim

class MyGovProject {

    final String VERSION = '1.0.${PROMOTED_ID}'

    DslFactory dsl

    PrintStream out

    List sites

    String name

    String repo

    String host

    String site

    String debian

    String maven

    Job build(DslFactory dslFactory, sites, out) {
        this.dsl = dslFactory
        this.sites = sites
        this.out = out
        try {
            return buildJob()
        } catch (Throwable t) {
            t.printStackTrace(out)
            throw t;
        }
    }

    Job buildJob() {
        return dsl.job(slug(name)) {
            displayName(this.name)
            logRotator {
                daysToKeep(90)
            }
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
            triggers {
                scm('# Poll SCM enabled to allow trigger from git hook.')
            }
            publishers {
                publish(delegate)
                slack(delegate)
            }
            properties {
                deploy(delegate)
            }
            configure { job ->
                job / scm / clean(clean())
            }
        }
    }

    def boolean clean() {
        return true
    }

    def void build(def StepContext delegate) {

    }

    def void publish(def PublisherContext delegate) {

    }

    def void slack(def PublisherContext delegate) {
        delegate.slackNotifications {
            notifyAborted()
            notifyFailure()
            notifyNotBuilt()
            notifyUnstable()
            notifyBackToNormal()
        }
    }

    def deploy(PropertiesContext properties) {
        if (!site || !debian) {
            return
        }
        def target = site == 'both' ?
            sites.collect { it.id } :
            [ site ]

        def devEnvs = sites.
            collect { it.environments }.
            grep { it.type == "isis" }.
            collect { it.name }

        def siteEnvs = sites.
            grep { target.contains(it.id) }.
            collect { it.environments }.
            collect { it.name }

        def envs = flatten(siteEnvs)

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
                            manual(null)
                        }
                    }
                    actions {
                        shell("pipeline deploy:${debian},${VERSION},${nm} sync")
                        if (isDev && host) {
                            shell(deploySshStep(nm + host, out))
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
            curl -sSf -o "${name}.deb" "${repo}/${path}"
            scp -o StrictHostKeyChecking=no "${name}.deb" "devops@${host}:/tmp/${name}.deb"
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
