package cl.dsy1103.ms_pedido.config;


import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "ms-catalogo")
public interface CatalogoClient {
    @GetMapping("/api/productos/{id}")
    ProductoInfo obtenerProducto(@PathVariable("id") Long productoId);

    @Data
    class ProductoInfo {
        private Long id;
        private String nombre;
        private BigDecimal precio;
    }

}
