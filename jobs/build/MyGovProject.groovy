package build

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.helpers.step.StepContext
import javaposse.jobdsl.dsl.helpers.properties.PropertiesContext
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext

import static build.Utils.repo
import static build.Utils.slug
import static build.Utils.trim

class MyGovProject {

    final String VERSION = '1.0.${PROMOTED_ID}'

    DslFactory dsl

    PrintStream out

    List sites

    /* Display name of the project */
    String name

    /* Slug of the git repository, without any path or .git extension */
    String repo

    /* Host name (without environment prefix) on which to deploy builds */
    String host

    /* Site that uses this project: mygov, gov, or both */
    String site

    /* Name of the Debian pacakge built by this job */
    String debian

    /* Maven coordinates, in the form groupId:artifactId  */
    String maven

    /* Remote git repository to push to after a successful build */
    String mirror

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
                if (mirror) {
                    shell("""\
                        git config remote.source.fetch +refs/*:refs/mirror/*
                        git config remote.source.url \$(git config remote.origin.url)
                        git fetch source
                        git config remote.target.url ${mirror}
                        git config remote.target.push refs/mirror/*:refs/*
                        git push --prune target
                    """.stripIndent())
                }
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
        delegate.slackNotifier {
            notifyAborted(true)
            notifyFailure(true)
            notifyNotBuilt(true)
            notifyUnstable(true)
            notifyBackToNormal(true)
            notifyRepeatedFailure(true)
        }
    }

    def String deployVersion(String debian, String env) {
        return "pipeline deploy:${debian},${VERSION},${env} sync"
    }

    def deploy(PropertiesContext properties) {
        if (!site || !debian) {
            return
        }
        def targets = site == 'both' ? sites : sites.grep { it.id == site }
        def envs = flatten(targets.collect { it.environments })

        def i = 0;
        properties.promotions {
            envs.each { env ->
                def nm = env.name
                promotion {
                    name(sprintf("%02d", i++) + " " + nm)
                    icon('star-gold')
                    conditions {
                        if (env.auto) {
                            selfPromotion(false)
                        } else {
                            manual(null)
                        }
                    }
                    actions {
                        shell(deployVersion(debian, nm))
                        if (env.auto && host) {
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
            repo=http://nexus/repository/releases/
            curl -sSf -o "${name}.deb" "${repo}/${path}"
            scp -o StrictHostKeyChecking=no "${name}.deb" "devops@${host}:/tmp/${name}.deb"
            ssh -o StrictHostKeyChecking=no devops@${host} "sudo dpkg -i /tmp/${name}.deb"
        ''')
        return script
    }

    /**
     * Flattens a map of lists: [[ma, mb, mc], [ga, gb]] -> [ma, ga, mb, gb, mc]
     */
    def <X> List<X> flatten(List<List<X>> lists) {
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
