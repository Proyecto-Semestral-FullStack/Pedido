package cl.dsy1103.ms_pedido.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class InventarioClient {
    private final WebClient webClient;

    public InventarioClient(@LoadBalanced WebClient.Builder builder,
                            @Value("${inventario.service.url}") String inventarioUrl) {
        this.webClient = builder.baseUrl(inventarioUrl).build();
    }

    public boolean verificarStock(Long productoId, int cantidad) {
        try {
            Map<String, Object> resp = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/stock/disponible")
                            .queryParam("productoId", productoId)
                            .queryParam("cantidad", cantidad)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return resp != null && (boolean) resp.getOrDefault("disponible", false);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean descontarStock(Long productoId, int cantidad, String observacion) {
        try {
            // 1. Obtener stockId
            Map<String, Object> stockResp = webClient.get()
                    .uri("/api/stock/producto/{productoId}", productoId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (stockResp == null) return false;
            int stockId = (Integer) stockResp.get("id");

            // 2. Descontar
            Map<String, Object> body = Map.of(
                    "stockId", stockId,
                    "cantidad", cantidad,
                    "observacion", observacion
            );
            webClient.put()
                    .uri("/api/stock/disminuir")
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
