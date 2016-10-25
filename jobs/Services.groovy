import services.Promotion

def jobs = []
Binding binding = new Binding()
binding.setVariable("dsl", this)
binding.setVariable("out", out)
jobs << new Promotion(binding).build()

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
