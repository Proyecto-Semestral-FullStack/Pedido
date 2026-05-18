package cl.dsy1103.ms_pedido.repository;

import cl.dsy1103.ms_pedido.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
}