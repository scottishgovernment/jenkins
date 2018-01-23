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

    /* Site that uses this project: mygov, gov, or both */
    String site

    /* Artifacts by package name, if this is job creates multiple artifacts */
    Map<String, Artifact> artifacts

    /* Host name (without environment prefix) on which to deploy builds */
    String host

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

    def deploy(PropertiesContext properties) {
        if (!site || !(debian || artifacts)) {
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
                        def artifacts = artifacts()
                        shell(pipelineDeploy(artifacts, nm))
                        if (env.auto) {
                            shell(sshDeploy(artifacts, nm))
                        }
                    }
                }
            }
        }
    }

    def artifacts() {
        def artifacts = []
        if (this.artifacts) {
            artifacts += this.artifacts.values()
        }
        if (debian && maven && host) {
            artifacts.add(new Artifact([
                debian: debian,
                maven: maven,
                host: host
            ]))
        }
        return artifacts
    }

    def String pipelineDeploy(List<Artifact> artifacts, String env) {
        def script = new StringBuilder()
        def delimiter = artifacts.size() == 1 ? ' ' : ' \\\n  '
        script << 'pipeline' << delimiter
        script << artifacts.collect {
            "deploy:${it.debian},${VERSION},${env}"
        }.join(delimiter)
        script << delimiter
        script << 'sync\n'
        return script.toString()
    }

    def String sshDeploy(List<Artifact> artifacts, String env) {
        def script = new StringBuilder()
        script << "repo=http://nexus/repository/releases/\n"
        artifacts.collect {
            deployArtifactBySSH(it, env, script)
        }
        return script.toString()
    }

    def String deployArtifactBySSH(Artifact artifact, String env, StringBuilder script) {
        def debian = artifact.debian
        def maven = artifact.maven
        def host = env + artifact.host
        def colon = maven.indexOf(':')
        def groupId = maven.substring(0, colon)
        def artifactId = maven.substring(colon + 1)

        def path = new StringBuilder()
        path << groupId.replace('.', '/') << '/'
        path << artifactId << '/'
        path << VERSION << '/'
        path << artifactId << '-' << VERSION << '.deb'

        script << "\n"
        script << trim("""\
            curl -sSf -o "${debian}.deb" "\${repo}/${path}"
            scp -o StrictHostKeyChecking=no "${debian}.deb" "devops@${host}:/tmp/${debian}.deb"
            ssh -o StrictHostKeyChecking=no devops@${host} "sudo dpkg -i /tmp/${debian}.deb"
            """)
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
