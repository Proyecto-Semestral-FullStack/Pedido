package cl.dsy1103.ms_pedido.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoPedidoRequest {
    @NotNull(message = "estadoPedido es obligatorio")
    private String estadoPedido;
}