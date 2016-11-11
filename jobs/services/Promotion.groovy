package services

import static build.Utils.trim

def build() {
    dsl.job('promote-services') {
        displayName("Promote Services Puppet")
        parameters {
            stringParam('override', '',
                "If the required version isn't available above, specify it here.")
        }

        steps {
          shell(trim('''\
            if [ -z "$override" ]; then
              pipeline promote:dev,services,puppetry
            else
              pipeline deploy:dev,puppetry,${override}
            fi
            pipeline sync
            '''))
        }

        configure {
            params = (it / 'properties'
                / 'hudson.model.ParametersDefinitionProperty'
                / 'parameterDefinitions')
                .children()
            params.add(0, 'hudson.plugins.promoted__builds.parameters.PromotedBuildParameterDefinition' {
                name('version')
                description('')
                projectName('puppet-manifests')
                promotionProcessName('Default')
            })
        }

    }
}
