package cl.dsy1103.ms_pedido.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Solo guardamos el id del usuario (no relación JPA con ms-usuarios)
    @Column(nullable = false)
    private Long usuarioId;

    //@Column(nullable = false)  futura implementacion con ms-envios no implementado aun.
    //Se puede dejar un valor por defecto para pruebas.
    private Long direccionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estadoPedido;

    @Column(nullable = false, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, scale = 2)
    private BigDecimal descuento;

    @Column(nullable = false, scale = 2)
    private BigDecimal total;

    @Column(length = 1000)
    private String notas;

    @Column(nullable = false)
    private OffsetDateTime creadoEn;

    @Column(nullable = false)
    private OffsetDateTime actualizadoEn;

    // Relación interna entre Pedido y DetallePedido (OneToMany).
    // Permitimos cascade PERSIST para guardar detalles cuando guardamos el pedido.
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetallePedido> detalles = new ArrayList<>();
}