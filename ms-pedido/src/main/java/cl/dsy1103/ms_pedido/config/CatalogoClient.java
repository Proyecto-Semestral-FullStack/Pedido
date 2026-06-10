package cl.dsy1103.ms_pedido.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
public class CatalogoClient {
    private final WebClient webClient;
    public CatalogoClient(@LoadBalanced WebClient.Builder builder,
                          @Value("${catalogo.service.url}") String catalogoUrl) {
        this.webClient = builder.baseUrl(catalogoUrl).build();
    }

    public ProductoInfo obtenerProducto(Long productoId) {
        return webClient.get()
                .uri("/api/productos/{id}", productoId)
                .retrieve()
                .bodyToMono(ProductoInfo.class)
                .block();
    }

    public static class ProductoInfo {
        private Long id;
        private String nombre;
        private BigDecimal precio;
        // getters y setters (puedes usar Lombok @Data)
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public BigDecimal getPrecio() { return precio; }
        public void setPrecio(BigDecimal precio) { this.precio = precio; }
    }

}
