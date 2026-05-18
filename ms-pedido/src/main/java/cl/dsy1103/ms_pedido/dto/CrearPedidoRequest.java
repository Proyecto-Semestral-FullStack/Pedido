package cl.dsy1103.ms_pedido.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearPedidoRequest {
    @NotNull(message = "usuarioId es obligatorio")
    private Long usuarioId;

    @NotNull(message = "direccionId es obligatorio")
    private Long direccionId;

    @Size(min = 1, message = "Debe existir al menos un detalle")
    private List<DetallePedidoRequest> detalles;

    // Descuento en valor absoluto (ej: 100.00)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal descuento;

    @Size(max = 1000)
    private String notas;
}