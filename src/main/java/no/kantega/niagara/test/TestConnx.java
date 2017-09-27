package no.kantega.niagara.test;

import io.vertx.core.http.HttpClientResponse;
import no.kantega.niagara.work.Client;
import org.kantega.niagara.Task;

import java.net.URISyntaxException;
import java.time.Duration;

public class TestConnx {

    public static void main(String[] args) throws URISyntaxException {

        Task<HttpClientResponse> resp = Client.get(8080,"localhost","/jalla/balla");
        Task<HttpClientResponse> resp2 = resp.flatMap(response->Client.get(8080,"localhost","/participant/balla"));
        resp2.execute().await(Duration.ofSeconds(2));

    }
}
