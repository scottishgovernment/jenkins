import org.yaml.snakeyaml.Yaml

import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.trim

def yaml = new Yaml().load(readFileFromWorkspace("resources/environments.yaml"))
def environmentsView = []
def pipelineView = []
def sites = yaml.get("sites")

scripts = [
    'mygov': [
        'full': [
            'up': 'tools/provisioning/vpc/aws_build_full_env.sh ${env}',
            'down': 'tools/provisioning/vpc/aws_teardown_full_env.sh ${env}_vpc'
        ],
        'test': [
            'up': 'tools/provisioning/vpc/aws_build_env.sh ${env}',
            'down': 'tools/provisioning/vpc/aws_teardown_env.sh ${env}_vpc'
        ]
    ],
    'gov': [
        'test': [
            'up': 'tools/provisioning/vpc/aws_build_env_govscot.sh ${env}',
            'down': 'tools/provisioning/vpc/aws_teardown_env_govscot.sh ${env}_vpc'
        ]
    ]
]

def envUp(site, type, List<String> envs) {
    def script = scripts.get(site.id)?.get(type)?.get('up')
    return job("${site.id}-${type}-up") {
        displayName("Build ${site.domain} ${type} environment")
        scm {
            git(repo('aws'))
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

def envDown(site, type, List<String> envs) {
    def script = scripts[site.id]?.get(type)?.get('down')
    return job("${site.id}-${type}-down") {
        displayName("Tear down ${site.domain} ${type} environment")
        scm {
            git(repo('aws'))
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
            choiceParam('redisrestore', ['false', 'true'], 'restore redis and images')
        }
        scm {
            git(repo('aws'))
        }
        steps {
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
        name('version-capture')
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
