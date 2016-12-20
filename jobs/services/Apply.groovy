package services

import static build.Utils.trim

def build() {

    def template = dsl.readFileFromWorkspace('resources/puppet-apply-services')

    dsl.job('puppet-services') {

        displayName("Apply Services Puppet")

        steps {
            def script = StringBuilder.newInstance()
            script << trim("""\
                #!/bin/sh
                set -e
                cat <<'APPLY' > apply
                """)
            script << template
            script << trim('''\
                APPLY

                chmod +x apply
                set -x
                SSH_OPTS="-o StrictHostKeyChecking=no"
                hostname=(jira.digital.gov.uk stash.digital.gov.uk repo.digital.gov.uk confluence.digital.gov.uk sonar.digital.gov.uk nagios.digital.gov.uk)
                echo "${hostname[@]}"
                for host in "${hostname[@]}" ; do
                scp $SSH_OPTS apply devops@${host}:/tmp
                ssh $SSH_OPTS devops@${host} /tmp/apply
                exit 1
                done
                ''')
            shell(script.toString())
        }

    }

}
