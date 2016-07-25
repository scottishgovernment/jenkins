import org.yaml.snakeyaml.Yaml
import environments.Puppet
import environments.VPC

import static build.Utils.trim
import static build.Utils.awsRepo

def yaml = new Yaml().load(readFileFromWorkspace("resources/environments.yaml"))
def environmentsView = []
def pipelineView = []
def sites = yaml.sites

def vpc = new VPC(this, out)
def puppet = new Puppet(this, out)

def promote(site, List<String> envs) {
    return job("promote-${site.id}") {
        displayName("Promote ${site.domain}")
        parameters {
            choiceParam('from', envs, 'Get versions from this environment')
            choiceParam('to',   envs.drop(1), 'Stage versions in this environment')
        }
        steps {
            shell('pipeline promote:${from},${to} sync')
        }
        publishers {
            buildDescription('', '${from} - ${to}')
        }
    }
}

def s3copy(site, List<String> envs) {
    return job("copy-s3-${site.id}") {
        displayName("Restore S3 data for ${site.domain}")
        parameters {
            choiceParam('env', envs, 'Environment to which production S3 data is copied')
        }
        scm {
            awsRepo(delegate)
        }
        steps {
            shell("tools/management/s3_restore ${site.domain} \${env}")
        }
        publishers {
            buildDescription('', '${env}')
        }
    }
}


def s3revert(site, List<String> envs) {
    def script = StringBuilder.newInstance()
    script << trim("""\
        #!/bin/sh
        set -ex
        id="${site.id}"
    """)
    script << trim('''\
        aws s3api list-object-versions --bucket ${id}-${env} --output json \
          --query "Versions[?LastModified>=\\`${date}T${time}\\`].[Key, VersionId]" | \
          jq -r '.[] | "--key '\\\''" + .[0] + "'\\\'' --version-id " + .[1]' | \
          xargs -L1 aws s3api delete-object --bucket ${id}-${env}
    ''')
    return job("${site.id}-revert-s3-bucket") {
        displayName("Revert ${site.domain} bucket to previous date/time")
        parameters {
            choiceParam('env', envs, "${site.domain} bucket")
            stringParam('date', '',
                "Date in format YYYY-MM-DD, e.g. 2016-06-08")
            stringParam('time', '',
                "Time in format HH:MM, e.g. 09:30")
        }
        steps {
            shell(script.toString())
        }
        publishers {
            buildDescription('', '${env}')
        }
    }
}

sites.collect { site ->
    out.println("Processing site ${site.domain}")
    environmentsView += vpc.site(site)

    def envNames = site.environments.collect { it.name }
    pipelineView << puppet.build(site, envNames)
    pipelineView << promote(site, envNames)
    pipelineView << s3copy(site, envNames)
    pipelineView << s3revert(site, envNames)
}

pipelineView << job('sync-repo') {
    displayName('Update S3 repository')
    steps {
        shell('pipeline sync')
    }
}

listView('Environments') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        environmentsView.each {
            name(it.name)
        }
    }
    columns {
        status()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}

listView('Pipeline') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        pipelineView.each {
            name(it.name)
        }
    }
    columns {
        status()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}
