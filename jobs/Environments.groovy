import org.yaml.snakeyaml.Yaml

import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.trim

def yaml = new Yaml().load(readFileFromWorkspace("resources/environments.yaml"))
def jobs = yaml.get("jobs")

jobs.collect { job ->

    out.println("Processing job ${job['name']}")

    delegate.job(job['id']) {
        displayName(job['name'])
        scm {
            git(repo('aws'))
        }
        parameters {
            job['parameters'].each { p ->
              choiceParam(p['name'], p['options'], p['description'])
            }
        }
        steps {
            shell(trim("""\
                set -e
                echo ${job['script']}
                echo "INFO: Completed: ${name}" \
            """))
        }
        publishers {
            buildDescription('', '${env}')
        }
    }

}

listView('Environments') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        jobs.each {
            name(it['id'])
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
