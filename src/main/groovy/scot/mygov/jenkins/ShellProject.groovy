package scot.mygov.jenkins

import javaposse.jobdsl.dsl.helpers.step.StepContext

class ShellProject extends MyGovProject {

    String build

    def void build(def StepContext delegate) {
        delegate.shell(dsl.readFileFromWorkspace('resources/' + build))
    }

}
