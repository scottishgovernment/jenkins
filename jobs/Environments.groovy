import org.yaml.snakeyaml.Yaml

import static build.Utils.trim
import static build.Utils.awsRepo

def yaml = new Yaml().load(readFileFromWorkspace("resources/environments.yaml"))
def environmentsView = []
def pipelineView = []
def sites = yaml.get("sites")

scripts = [
    'mygov': [
        'full': [
            'up': 'vpc/mygov_build_full.sh ${env} ${ami}',
            'down': 'vpc/mygov_teardown_full.sh ${env}_vpc'
        ],
        'test': [
            'up': 'vpc/mygov_build_test.sh ${env} ${ami}',
            'down': 'vpc/mygov_teardown_test.sh ${env}_vpc'
        ]
    ],
    'gov': [
        'full': [
            'up': 'vpc/gov_build_full.sh ${env} ${ami}',
            'down': 'vpc/gov_teardown_full.sh ${env}_vpc'
        ],
        'test': [
            'up': 'vpc/gov_build_test.sh ${env} ${ami}',
            'down': 'vpc/gov_teardown_test.sh ${env}_vpc'
        ]
    ]
]

def envUp(site, type, List<String> envs) {
    def cmds = StringBuilder.newInstance()
    cmds << "#!/bin/sh -e\n"
    cmds << "ami=\${override:-\$version_NUMBER}\n\n"
    cmds << "tools/management/s3_restore ${site.domain} \${env}\n"
    cmds << scripts.get(site.id)?.get(type)?.get('up') << '\n'

    return job("${site.id}-${type}-up") {
        displayName("Build ${site.domain} ${type} environment")
        scm {
            awsRepo(delegate)
        }
        parameters {
            choiceParam('env', envs, "${site.domain} environment")
        }
        steps {
            shell(cmds.toString())
        }
        publishers {
            buildDescription('', '${env}')
        }
        parameters {
            stringParam('override', '',
                "If the required version isn't available above, specify it here.")
        }
        configure {
            params = (it / 'properties'
                / 'hudson.model.ParametersDefinitionProperty'
                / 'parameterDefinitions')
                .children()

            params.add(0, 'hudson.plugins.promoted__builds.parameters.PromotedBuildParameterDefinition' {
                name('version')
                description('')
                projectName("${site.id}-ami")
                promotionProcessName('Default')
            })
        }
    }
}

def envDown(site, type, List<String> envs) {
    def script = scripts[site.id]?.get(type)?.get('down')
    return job("${site.id}-${type}-down") {
        displayName("Tear down ${site.domain} ${type} environment")
        scm {
            awsRepo(delegate)
        }
        parameters {
            choiceParam('env', envs, "${site.domain} environment")
        }
        steps {
            shell(script)
        }
        publishers {
            buildDescription('', '${env}')
        }
    }
}

def puppet(site, List<String> envs) {
    return job("puppet-${site.id}") {
        displayName("Puppet Apply - ${site.domain}")
        parameters {
            choiceParam('env', envs, "${site.domain} environment")
            choiceParam('dbrestore', ['false', 'true'], 'restore databases')
            if (site.domain == "gov.scot" ) {
              choiceParam('redisrestore', ['false', 'true'], 'restore redis and images')
            }
        }
        scm {
            awsRepo(delegate)
        }
        steps {
            shell((trim("""\
              if [ "\$dbrestore" = "true" ]; then
                tools/management/s3_restore ${site.domain} \${env}
              fi
            """)))
            shell(readFileFromWorkspace('resources/puppet-apply.sh'))
        }
    }
}

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

    def environments = site.environments
    def types = environments.collect { it.type }.unique(false)
    types.collect { type ->
        def envs = environments.grep { it.type == type }.collect { it.name }
        if (scripts[site.id]?.get(type)?.get('up')) {
            environmentsView << envUp(site, type, envs)
        }
        if (scripts[site.id]?.get(type)?.get('down')) {
            environmentsView << envDown(site, type, envs)
        }
    }

    def envNames = environments.collect { it.name }
    pipelineView << puppet(site, envNames)
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
