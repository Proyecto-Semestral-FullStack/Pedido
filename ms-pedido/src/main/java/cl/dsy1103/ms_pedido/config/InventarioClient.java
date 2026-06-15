package cl.dsy1103.ms_pedido.config;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "ms-inventario")
public interface InventarioClient {
    @GetMapping("/api/stock/disponible")
    Map<String, Object> verificarStock(@RequestParam("productoId") Long productoId,
                                       @RequestParam("cantidad") int cantidad);

    // Este método se usará para descontar stock después del pago
    @PutMapping("/api/stock/disminuir")
    void descontarStock(@RequestBody Map<String, Object> body);
}
