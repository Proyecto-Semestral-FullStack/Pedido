package cl.dsy1103.ms_pedido.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "ms-pago")
public interface PagoClient {

    @PostMapping("/api/pagos")
    Map<String, Object> procesarPago(@RequestBody Map<String, Object> body);
}
