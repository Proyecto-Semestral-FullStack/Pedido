package cl.dsy1103.ms_pedido.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${ms.usuarios.base-url:http://localhost:8081}")
    private String usuariosBaseUrl;

    @Value("${ms.catalogo.base-url:http://localhost:8082}")
    private String catalogoBaseUrl;

    @Value("${ms.stock.base-url:http://localhost:8083}")
    private String stockBaseUrl;

    @Value("${ms.pago.base-url:http://localhost:8084}")
    private String pagoBaseUrl;

    @Bean("usuariosWebClient")
    public WebClient usuariosWebClient() {
        return WebClient.builder().baseUrl(usuariosBaseUrl).build();
    }

    @Bean("catalogoWebClient")
    public WebClient catalogoWebClient() {
        return WebClient.builder().baseUrl(catalogoBaseUrl).build();
    }

    @Bean("stockWebClient")
    public WebClient stockWebClient() {
        return WebClient.builder().baseUrl(stockBaseUrl).build();
    }

    @Bean("pagoWebClient")
    public WebClient pagoWebClient() {
        return WebClient.builder().baseUrl(pagoBaseUrl).build();
    }
}