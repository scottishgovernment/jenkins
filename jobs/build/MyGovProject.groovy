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

    DslFactory dsl

    PrintStream out

    List sites

    /* Display name of the project */
    String name

    String version = '1.0.${BUILD_ID}'

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
                        git config remote.target.url ${mirror}
                        git fetch target refs/heads/dependabot/*:refs/remotes/target/dependabot/*
                        git push --tags --prune target \\
                          +refs/remotes/origin/*:refs/heads/* \\
                          refs/remotes/target/*:refs/heads/*
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
            baseUrl(null)
            commitInfoChoice('NONE')
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
                        def packages = artifacts.keySet().toList()
                        shell(pipelineDeploy(packages, nm))
                        if (env.auto && artifacts?.findResult { k, v -> v.hosts }) {
                            shell(sshDeploy(artifacts.values().toList(), nm))
                        }
                    }
                }
            }
        }
    }

    def String pipelineDeploy(List<String> packages, String env) {
        def promotedVersion = version.replace('BUILD_ID', 'PROMOTED_ID')
        def script = new StringBuilder()
        def delimiter = artifacts.size() == 1 ? ' ' : ' \\\n  '
        script << 'pipeline' << delimiter
        script << packages.collect {
            "deploy:${it},${promotedVersion},${env}"
        }.join(delimiter)
        script << delimiter
        script << 'sync\n'
        return script.toString()
    }

    def String sshDeploy(List<Artifact> artifacts, String env) {
        def script = new StringBuilder()
        def promotedVersion = version.replace('BUILD_ID', 'PROMOTED_ID')

        def hosts = artifacts
            .collect { it.hosts }
            .flatten()
            .toUnique()

        for (host in hosts) {
            def packages = artifacts
                .grep { it.hosts?.contains(host) }
                .collect { "${it.debian}=${promotedVersion}"}
                .join(' ')
            script << trim("""\
                ssh devops@${env}${host} /bin/sh -eux <<EOS
                    sudo apt-get update \\
                      -o Dir::Etc::sourcelist="sources.list.d/scotgov.list" \\
                      -o Dir::Etc::sourceparts="-" \\
                      -o APT::Get::List-Cleanup="0"
                    sudo apt-get install \\
                      --assume-yes \\
                      --allow-downgrades \\
                      ${packages}
                EOS
                """)
        }
        return script.toString()
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
