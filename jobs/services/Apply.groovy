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
                for host in macfs; do
                  scp $SSH_OPTS apply devops@${host}:/tmp
                  ssh $SSH_OPTS devops@${host} /tmp/apply
                done
                ''')
            shell(script.toString())
        }

    }

}
