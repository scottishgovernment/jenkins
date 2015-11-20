import static scot.mygov.jenkins.Utils.repo

job('mygov-release-prepare') {
    displayName('Prepare mygov.scot release')
    steps {
        shell('pipeline prepare:per,scot.mygov.release,mygov-scot,${BUILD_ID}')
    }
    properties {
         promotions {
              promotion {
                   name("Default")
                   icon("star-blue")
                   conditions {
                        selfPromotion()
                   }
              }
         }
    }
}

job('mygov-release-perform') {
    displayName('Perform mygov.scot release')

    parameters {
        choiceParam('env', ['blu', 'grn'], 'mygov.scot production environment')
        stringParam('override', '',
            "If the required version isn't available above, specify it here.")
    }

    steps {
        shell('pipeline perform:${env},scot.mygov.release,mygov-scot,${override:-$version_NUMBER}')
    }

    publishers {
        buildDescription('', '${env} - ${version.number}')
    }

    configure {
        params = (it / 'properties'
            / 'hudson.model.ParametersDefinitionProperty'
            / 'parameterDefinitions')
            .children()

        params.add(0, 'hudson.plugins.promoted__builds.parameters.PromotedBuildParameterDefinition' {
            name('version')
            description('')
            projectName('mygov-release-prepare')
            promotionProcessName('Default')
        })

    }

}
