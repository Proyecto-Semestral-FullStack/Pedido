package cl.dsy1103.ms_pedido.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePedidoRequest {
    @NotNull(message = "productoId es obligatorio")
    private Long productoId;

    @NotNull
    @Positive(message = "cantidad debe ser mayor que 0")
    private Integer cantidad;

    // opcional: precioUnitario puede venir del catálogo, pero permitimos que lo envíen para pruebas
    @Positive(message = "precioUnitario debe ser positivo")
    private BigDecimal precioUnitario;
}