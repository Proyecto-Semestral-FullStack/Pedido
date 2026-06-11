package cl.dsy1103.ms_pedido.service;

import cl.dsy1103.ms_pedido.dto.*;
import java.util.List;

public interface PedidoService {
    List<PedidoResponse> listarTodos();
    PedidoResponse obtenerPorId(Long id);
    List<PedidoResponse> obtenerPorUsuario(Long usuarioId);
    PedidoResponse crearPedido(CrearPedidoRequest request);
    PedidoResponse cambiarEstado(Long id, CambiarEstadoPedidoRequest request);
    void eliminarPedido(Long id);
    String health();
    boolean existeCompra(Long usuarioId, Long productoId);
}