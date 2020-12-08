package environments

import static build.Utils.awsRepo

def build(site, List<String> envs) {
    dsl.job("whitelist-${site.id}") {

        displayName("Whitelist IPs for ${site.domain}")

        parameters {
            choiceParam('env', envs, "${site.domain} environment")
            choiceParam('resource', [
                'cms',
                'site',
                'both'
                ], 'resource to whitelist')
            stringParam('cidr', '', 'Network (in CIDR notation) or IP address')
        }

        logRotator {
          daysToKeep(90)
        }

        scm {
            awsRepo(delegate)
        }

        steps {
            shell('tools/whitelist "$env" "$resource" "$cidr"')
        }

    }
}
