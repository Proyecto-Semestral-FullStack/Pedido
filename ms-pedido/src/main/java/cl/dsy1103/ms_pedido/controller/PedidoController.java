package cl.dsy1103.ms_pedido.controller;

import cl.dsy1103.ms_pedido.dto.*;
import cl.dsy1103.ms_pedido.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PedidoResponse>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(pedidoService.obtenerPorUsuario(usuarioId));
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crearPedido(@Valid @RequestBody CrearPedidoRequest request) {
        PedidoResponse response = pedidoService.crearPedido(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/verificar")
    public ResponseEntity<Boolean> verificarCompra(
            @RequestParam Long usuarioId,
            @RequestParam Long productoId) {
        return ResponseEntity.ok(pedidoService.existeCompra(usuarioId, productoId));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<PedidoResponse> cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoPedidoRequest request) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoService.eliminarPedido(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok(pedidoService.health());
    }
}