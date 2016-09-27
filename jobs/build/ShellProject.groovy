package build

import javaposse.jobdsl.dsl.helpers.step.StepContext

import static build.Utils.trim

class ShellProject extends MyGovProject {

    String clean

    String build

    def boolean clean() {
        return !clean
    }

    def void build(def StepContext delegate) {
        def script = StringBuilder.newInstance()
        script << trim("""\
            #!/bin/sh
            set -ex
            repo=${repo}
            """)
        if (maven) {
            def splits = maven.split(':')
            def groupId = splits[0]
            def artifactId = splits[1]
            script << trim("""\
                debian=${debian}
                groupId=${groupId}
                artifactId=${artifactId}
                version="1.0.\${BUILD_ID}"\n
                """)
        }
        if (clean) {
          script << clean << '\n\n'
        }
        script << trim('''\
            git tag -a -m "Build ${version}" ${version}
            git push --tags ssh://git@stash.digital.gov.uk:7999/mgv/${repo}.git "${version}"
            ''')

        if (build) {
            script << build << '\n\n'
        }

        if (maven) {
          script << trim('''\
              mvn deploy:deploy-file \\
                --batch-mode \\
                -Dfile="${debian}_${version}_all.deb" \\
                -DgroupId="${groupId}" \\
                -DartifactId="${artifactId}" \\
                -Dversion="${version}" \\
                -Dpackaging=deb \\
                -DrepositoryId=release \\
                -Durl=http://repo.digital.gov.uk/content/repositories/releases/
              ''')
        }

        delegate.shell(script.toString())
    }

}
