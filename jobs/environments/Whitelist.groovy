package environments

import static build.Utils.awsRepo

def build(site, List<String> envs) {
    dsl.job("whitelist-${site.id}") {

        displayName("Whitelist IPs for ${site.domain}")

        parameters {
            choiceParam('env', envs, "${site.domain} environment")
            choiceParam('resource', [
                'both',
                'cms',
                'site'
                ], 'resource to whitelist. ("both" will whitelist for site and cms)')
            stringParam('cidr', '',
                'Network in CIDR notation (e.g. 13.248.154.0/24) or IP address')
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
