package uk.gov.cshr.service.jobs;

public abstract class ConfiguredJob<C extends BaseJobConfig> implements Job {

    protected final C config;

    public ConfiguredJob(C config) {
        this.config = config;
    }

    @Override
    public String getCron() {
        return config.getCron();
    }

    @Override
    public boolean isEnabled() {
        return config.getEnabled();
    }

}
