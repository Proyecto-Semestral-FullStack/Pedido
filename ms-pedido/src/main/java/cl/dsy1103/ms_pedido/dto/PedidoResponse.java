package cl.dsy1103.ms_pedido.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResponse {
    private Long id;
    private Long usuarioId;
    private Long direccionId;
    private String estadoPedido;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal total;
    private String notas;
    private OffsetDateTime creadoEn;
    private OffsetDateTime actualizadoEn;
    private List<DetallePedidoResponse> detalles;
}
