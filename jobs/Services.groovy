import services.Promotion
import services.Apply
import services.Devnet

Binding binding = new Binding()
binding.setVariable("dsl", this)
binding.setVariable("out", out)

def promotion = new Promotion()
def apply = new Apply()
def devnet = new Devnet()

def jobs = []
[promotion, apply, devnet].each { job ->
  job.setBinding(binding)
  jobs << job.build()
}

listView('Services') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        jobs.each {
            name(it.name)
        }
    }
    columns {
        status()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}
