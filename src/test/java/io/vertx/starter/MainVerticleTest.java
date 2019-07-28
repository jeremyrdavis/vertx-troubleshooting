package io.vertx.starter;


import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.assertj.core.api.Assertions.*;


import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class MainVerticleTest {

  private Vertx vertx;

  @Test
  @DisplayName("Test Server Starts Using HttpClient")
  public void testWithHttpClient(Vertx vertx, VertxTestContext tc) {

    final HttpClient httpClient = vertx.createHttpClient();

    Checkpoint deploymentCheckpoint = tc.checkpoint();
    Checkpoint requestCheckpoint = tc.checkpoint();

    vertx.deployVerticle(new MainVerticle(), tc.succeeding(id -> {

      deploymentCheckpoint.flag();

      httpClient.request(HttpMethod.GET,80, "localhost", "/", resp -> {
        resp.bodyHandler(body -> {
          assertThat(body.toString()).contains("Hello, Vert.x!");
          requestCheckpoint.flag();
        });
      });

    }));

  }

  @Test
  @DisplayName("Test Server Starts Using WebClient")
  public void testWithWebClient(Vertx vertx, VertxTestContext tc) {

    WebClient webClient = WebClient.create(vertx);
    Checkpoint deploymentCheckpoint = tc.checkpoint();
    Checkpoint requestCheckpoint = tc.checkpoint();

    vertx.deployVerticle(new MainVerticle(), tc.succeeding(id -> {

      deploymentCheckpoint.flag();

      webClient.get(8080, "localhost", "/")
        .as(BodyCodec.string())
        .send(tc.succeeding(resp -> {
          tc.verify(() -> {
            assertThat(resp.body()).contains("Hello Vert.x!");
            assertThat(resp.statusCode()).isEqualTo(200);
            requestCheckpoint.flag();
          });
        }));
    }));

  }

}
