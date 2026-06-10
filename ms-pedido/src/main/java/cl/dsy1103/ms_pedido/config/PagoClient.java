package cl.dsy1103.ms_pedido.config;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class PagoClient {
    private final WebClient webClient;

    public PagoClient(@LoadBalanced WebClient.Builder builder,
                      @Value("${pago.service.url}") String pagoUrl) {
        this.webClient = builder.baseUrl(pagoUrl).build();
    }

    public boolean procesarPago(Long pedidoId, BigDecimal monto) {
        Map<String, Object> body = Map.of(
                "pedidoId", pedidoId,
                "monto", monto,
                "metodoPago", "TARJETA_CREDITO"
        );
        Map<String, Object> resp = webClient.post()
                .uri("/api/pagos")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return "APROBADO".equalsIgnoreCase((String) resp.getOrDefault("estado", ""));
    }
}
