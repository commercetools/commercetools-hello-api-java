import io.sphere.sdk.categories.Category;
import io.sphere.sdk.client.BlockingSphereClient;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import io.sphere.sdk.client.SphereClientFactory;
import io.sphere.sdk.products.ProductProjection;
import io.sphere.sdk.products.queries.ProductProjectionQuery;
import io.sphere.sdk.queries.PagedQueryResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.util.Locale.ENGLISH;
import static java.util.stream.Collectors.joining;

// This example uses the official Sphere Java client:
// https://github.com/sphereio/sphere-jvm-sdk

public class HelloApi {
    public static void main(String[] args) throws Exception {
        try(final BlockingSphereClient client = createCommercetoolsClient()) {
            final ProductProjectionQuery searchRequest =
                    ProductProjectionQuery.ofCurrent()
                            .withPredicates(m -> m.name().locale(ENGLISH).isPresent())
                            .withExpansionPaths(m -> m.categories())
                            .withLimit(10)
                            .withSort(m -> m.createdAt().sort().desc());
            final PagedQueryResult<ProductProjection> queryResult =
                    client.executeBlocking(searchRequest);
            for (final ProductProjection product : queryResult.getResults()) {
                final String output = productProjectionToString(product);
                System.out.println(output);
            }
        }
    }

    private static String productProjectionToString(final ProductProjection product) {
        final String name = product.getName().get(ENGLISH);
        final String categoryNamesString = product.getCategories()
                .stream()
                .filter(ref -> ref.getObj() != null)
                .map(categoryReference -> {
                    final Category category = categoryReference.getObj();
                    return category.getName().find(ENGLISH).orElse("name unknown");
                })
                .collect(joining(", "));
        return "found product " + name + " in categories " + categoryNamesString;
    }

    private static BlockingSphereClient createCommercetoolsClient() throws IOException {
        final Properties properties = loadPropertiesFromClasspath("commercetools.properties");
        final SphereClientConfig config = SphereClientConfig.ofProperties(properties, "");
        final SphereClient underlyingClient = SphereClientFactory.of().createClient(config);//this client only works asynchronous
        return BlockingSphereClient.of(underlyingClient, 10, TimeUnit.SECONDS);
    }

    private static Properties loadPropertiesFromClasspath(final String path) throws IOException {
        final Properties properties = new Properties();
        try (final InputStream stream = HelloApi.class.getResourceAsStream(path)) {
            properties.load(stream);
        }
        return properties;
    }
}
