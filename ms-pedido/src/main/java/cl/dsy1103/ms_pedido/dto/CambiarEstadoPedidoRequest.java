package cl.dsy1103.ms_pedido.dto;

import cl.dsy1103.ms_pedido.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoPedidoRequest {
    @NotNull(message = "estadoPedido es obligatorio")
    private EstadoPedido estadoPedido;
}
