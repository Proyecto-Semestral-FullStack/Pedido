package cl.dsy1103.ms_pedido.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class UsuarioClient {
    private final WebClient webClient;

    public UsuarioClient(@LoadBalanced WebClient.Builder builder,
                         @Value("${usuario.service.url}") String usuarioUrl) {
        this.webClient = builder.baseUrl(usuarioUrl).build();
    }

    public boolean existeUsuario(Long usuarioId) {
        try {
            webClient.get()
                    .uri("/api/usuarios/id/{id}", usuarioId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
