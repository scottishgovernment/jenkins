package scot.mygov.jenkins

import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.FileJobManagement
import javaposse.jobdsl.dsl.GeneratedItems
import javaposse.jobdsl.dsl.GeneratedJob
import javaposse.jobdsl.dsl.GeneratedView
import javaposse.jobdsl.dsl.ScriptRequest

import java.util.logging.Logger

class Run {

    private static final Logger LOG = Logger.getLogger(Run.name)

    static void main(String[] args) throws Exception {
        File jobs = new File('jobs')

        FileJobManagement manager = new FileJobManagement(new File('out')) {
            @Override
            String readFileInWorkspace(String filePath) {
                new File(filePath).text
            }
        }

        manager.parameters.putAll(System.getenv())
        System.properties.each { def key, def value ->
            manager.parameters.put(key.toString(), value.toString())
        }

        URL url = jobs.toURI().toURL()
        args.each { String script ->
            ScriptRequest request = new ScriptRequest(script, null, url, false)
            GeneratedItems items = DslScriptLoader.runDslEngine(request, manager)

            for (GeneratedJob job : items.jobs) {
                LOG.info("Generated job: ${job.jobName}")
            }
            for (GeneratedView view : items.views) {
                LOG.info("Generated view: ${view.name}")
            }
        }
    }

}
