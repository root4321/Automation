package org.test.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "api")
public class ApiProperties {
    private String baseUrl;
    private String key;
    private String defaultUpdateAddress;
    private Endpoints endpoints = new Endpoints();

    @PostConstruct
    void validate() {
        requireValue("api.base-url", baseUrl);
        requireValue("api.key", key);
        requireValue("api.default-update-address", defaultUpdateAddress);
        requireValue("api.endpoints.add", endpoints.getAdd());
        requireValue("api.endpoints.get", endpoints.getGet());
        requireValue("api.endpoints.update", endpoints.getUpdate());
        requireValue("api.endpoints.delete", endpoints.getDelete());
    }

    private void requireValue(String name, String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(name + " must be configured for the selected environment");
        }
    }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getDefaultUpdateAddress() { return defaultUpdateAddress; }
    public void setDefaultUpdateAddress(String defaultUpdateAddress) { this.defaultUpdateAddress = defaultUpdateAddress; }
    public Endpoints getEndpoints() { return endpoints; }
    public void setEndpoints(Endpoints endpoints) { this.endpoints = endpoints; }

    public static class Endpoints {
        private String add;
        private String get;
        private String update;
        private String delete;

        public String getAdd() { return add; }
        public void setAdd(String add) { this.add = add; }
        public String getGet() { return get; }
        public void setGet(String get) { this.get = get; }
        public String getUpdate() { return update; }
        public void setUpdate(String update) { this.update = update; }
        public String getDelete() { return delete; }
        public void setDelete(String delete) { this.delete = delete; }
    }
}
