package data

import static build.Utils.trim

def build(site, List<String> envs) {
    def script = StringBuilder.newInstance()
    script << trim("""\
        #!/bin/sh
        set -ex
        id="${site.id}"
        """)
    script << trim('''\
        aws s3api list-object-versions \\
          --bucket ${id}-${env} \\
          --output json \\
          --query "Versions[?LastModified>=\\`${date}T${time}\\`].[Key, VersionId]" | \\
          jq -r '.[] | "--key '\\\''" + .[0] + "'\\\'' --version-id " + .[1]' | \\
          xargs -L1 aws s3api delete-object --bucket ${id}-${env}
    ''')
    dsl.job("${site.id}-revert-s3-bucket") {
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
