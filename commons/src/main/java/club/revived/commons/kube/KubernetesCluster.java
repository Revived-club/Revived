package club.revived.commons.kube;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.jetbrains.annotations.NotNull;

public final class KubernetesCluster {

    private final KubernetesClient kubernetesClient;

    public KubernetesCluster(
            final String url,
            final String apiKey
    ) {
        this.kubernetesClient = this.init(url, apiKey);
    }

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

    public KubernetesClient getKubernetesClient() {
        return kubernetesClient;
    }
}
