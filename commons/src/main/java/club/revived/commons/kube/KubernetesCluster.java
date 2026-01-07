package club.revived.commons.kube;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.jetbrains.annotations.NotNull;

public final class KubernetesCluster {

    private final KubernetesClient kubernetesClient;

    /**
     * Creates a KubernetesCluster configured with the given cluster URL and API key.
     *
     * @param url    the Kubernetes API server base URL to use as the client master URL
     * @param apiKey the OAuth token used to authenticate requests to the cluster
     */
    public KubernetesCluster(
            final String url,
            final String apiKey
    ) {
        this.kubernetesClient = this.init(url, apiKey);
    }

    /**
     * Create a KubernetesClient configured for the specified master URL and OAuth token.
     *
     * @param url the Kubernetes master API server URL used to configure the client
     * @param apiKey the OAuth token used for authentication
     * @return a configured KubernetesClient instance
     */
    @NotNull
    private KubernetesClient init(
            final String url,
            final String apiKey
    ) {
        final Config config = new ConfigBuilder()
                .withOauthToken(apiKey)
                .withMasterUrl(url)
                .build();

        return new KubernetesClientBuilder()
                .withConfig(config)
                .build();
    }

    /**
     * Retrieves the encapsulated KubernetesClient instance.
     *
     * @return the internal KubernetesClient used by this cluster wrapper
     */
    public KubernetesClient getKubernetesClient() {
        return kubernetesClient;
    }
}