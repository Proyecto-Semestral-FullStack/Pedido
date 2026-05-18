package cl.dsy1103.ms_pedido.service;

import cl.dsy1103.ms_pedido.dto.*;
import cl.dsy1103.ms_pedido.exception.*;
import cl.dsy1103.ms_pedido.model.*;
import cl.dsy1103.ms_pedido.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    // Inyectamos los WebClient configurados
    private final WebClient usuariosWebClient;
    private final WebClient catalogoWebClient;
    private final WebClient stockWebClient;
    private final WebClient pagoWebClient;

    @Override
    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public PedidoResponse obtenerPorId(Long id) {
        return pedidoRepository.findById(id).map(this::mapToResponse)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado: " + id));
    }

    @Override
    public List<PedidoResponse> obtenerPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // Flujo principal:
    // 1) Validar usuario
    // 2) Validar productos y stock
    // 3) Crear pedido en BD en estado PENDIENTE
    // 4) Simular pago
    // 5) Si pago aprobado -> descontar stock y confirmar pedido
    // 6) Si pago rechazado -> cancelar pedido
    @Override
    public PedidoResponse crearPedido(CrearPedidoRequest request) {
        // 1) Validar usuario con ms-usuarios
        validateUsuarioExists(request.getUsuarioId());

        // 2) Validar productos y stock con ms-catalogo y ms-stock
        // Traemos datos de producto desde ms-catalogo (nombre, precio), y validamos stock.
        List<DetallePedidoRequest> detallesReq = request.getDetalles();

        // Consulta producto por producto (simple para el ejemplo). En producción, agrupar peticiones.
        Map<Long, ProductoInfo> productoInfoMap = new HashMap<>();
        for (DetallePedidoRequest detReq : detallesReq) {
            ProductoInfo pinfo = fetchProductoInfo(detReq.getProductoId());
            if (pinfo == null) {
                throw new NotFoundException("Producto no encontrado: " + detReq.getProductoId());
            }
            // Validar stock
            boolean stockOk = validateStock(detReq.getProductoId(), detReq.getCantidad());
            if (!stockOk) {
                throw new BadRequestException("Stock insuficiente para producto: " + detReq.getProductoId());
            }
            productoInfoMap.put(detReq.getProductoId(), pinfo);
        }

        // 3) Crear entidad Pedido en DB en estado PENDIENTE (primera transacción corta)
        Pedido pedido = Pedido.builder()
                .usuarioId(request.getUsuarioId())
                .direccionId(request.getDireccionId())
                .estadoPedido(EstadoPedido.PENDIENTE)
                .subtotal(BigDecimal.ZERO)
                .descuento(request.getDescuento() == null ? BigDecimal.ZERO : request.getDescuento())
                .total(BigDecimal.ZERO)
                .notas(request.getNotas())
                .creadoEn(OffsetDateTime.now())
                .actualizadoEn(OffsetDateTime.now())
                .build();

        // Convertir detalles y calcular subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetallePedidoRequest detReq : detallesReq) {
            ProductoInfo pinfo = productoInfoMap.get(detReq.getProductoId());
            BigDecimal precio = pinfo.getPrecio();
            int cantidad = detReq.getCantidad();
            BigDecimal detSubtotal = precio.multiply(BigDecimal.valueOf(cantidad));
            subtotal = subtotal.add(detSubtotal);

            DetallePedido detalle = DetallePedido.builder()
                    .pedido(pedido)
                    .productoId(detReq.getProductoId())
                    .nombreProducto(pinfo.getNombre())
                    .cantidad(cantidad)
                    .precioUnitario(precio)
                    .subtotal(detSubtotal)
                    .build();
            pedido.getDetalles().add(detalle);
        }

        pedido.setSubtotal(subtotal);
        BigDecimal total = subtotal.subtract(pedido.getDescuento() == null ? BigDecimal.ZERO : pedido.getDescuento());
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }
        pedido.setTotal(total);

        // Guardamos pedido (con detalles por CascadeType.ALL)
        Pedido pedidoGuardado = savePedidoShortTx(pedido);

        // 4) Simular pago (fuera de la tx corta)
        boolean pagoAprobado = simularPago(pedidoGuardado.getId(), pedidoGuardado.getTotal());

        if (pagoAprobado) {
            // 5) Descontar stock
            boolean stockDescontado = descontarStock(detallesReq);
            if (!stockDescontado) {
                // Si falla al descontar stock, marcamos CANCELADO y devolvemos error
                updateEstadoPedido(pedidoGuardado.getId(), EstadoPedido.CANCELADO);
                throw new ExternalServiceException("Fallo al descontar stock después del pago");
            }
            // 6) Actualizar estado a CONFIRMADO
            updateEstadoPedido(pedidoGuardado.getId(), EstadoPedido.CONFIRMADO);
        } else {
            // Pago rechazado -> CANCELADO
            updateEstadoPedido(pedidoGuardado.getId(), EstadoPedido.CANCELADO);
        }

        return obtenerPorId(pedidoGuardado.getId());
    }

    // Este método guarda el pedido en una transacción corta para evitar mantener transacción abierta durante llamadas externas
    @Transactional
    protected Pedido savePedidoShortTx(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    // Actualiza estado del pedido (transacción)
    @Transactional
    protected Pedido updateEstadoPedido(Long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow(() -> new NotFoundException("Pedido no encontrado: " + pedidoId));
        pedido.setEstadoPedido(nuevoEstado);
        pedido.setActualizadoEn(OffsetDateTime.now());
        return pedidoRepository.save(pedido);
    }

    @Override
    public PedidoResponse cambiarEstado(Long id, CambiarEstadoPedidoRequest request) {
        EstadoPedido nuevo;
        try {
            nuevo = EstadoPedido.valueOf(request.getEstadoPedido());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado inválido: " + request.getEstadoPedido());
        }
        Pedido pedido = updateEstadoPedido(id, nuevo);
        return mapToResponse(pedido);
    }

    @Override
    @Transactional
    public void eliminarPedido(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new NotFoundException("Pedido no encontrado: " + id);
        }
        pedidoRepository.deleteById(id);
    }

    @Override
    public String health() {
        return "ms-pedido OK";
    }

    // ---------- Métodos auxiliares para llamadas a microservicios (simplificados) ----------

    private void validateUsuarioExists(Long usuarioId) {
        try {
            usuariosWebClient.get()
                    .uri("/usuarios/{id}", usuarioId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.error("Error consultando ms-usuarios", e);
            throw new ExternalServiceException("Error consultando ms-usuarios: " + e.getMessage());
        }
    }

    private ProductoInfo fetchProductoInfo(Long productoId) {
        try {
            // Ejemplo: GET /productos/{id} retorna { id, nombre, precio }
            return catalogoWebClient.get()
                    .uri("/productos/{id}", productoId)
                    .retrieve()
                    .bodyToMono(ProductoInfo.class)
                    .block();
        } catch (Exception e) {
            log.error("Error consultando ms-catalogo para producto {}", productoId, e);
            throw new ExternalServiceException("Error consultando ms-catalogo");
        }
    }

    private boolean validateStock(Long productoId, Integer cantidad) {
        try {
            // Ejemplo: GET /stock/{productoId}/validar?cantidad=5 -> true/false
            return stockWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/stock/{id}/validar").queryParam("cantidad", cantidad).build(productoId))
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        } catch (Exception e) {
            log.error("Error consultando ms-stock", e);
            throw new ExternalServiceException("Error consultando ms-stock");
        }
    }

    private boolean descontarStock(List<DetallePedidoRequest> detalles) {
        try {
            // Convertir a lista de maps con casteo explícito
            List<Map<String, Object>> body = new ArrayList<>();
            for (DetallePedidoRequest d : detalles) {
                Map<String, Object> map = new HashMap<>();
                map.put("productoId", d.getProductoId());
                map.put("cantidad", d.getCantidad());
                body.add(map);
            }

            Boolean result = stockWebClient.post()
                    .uri("/stock/descontar")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return result != null && result;
        } catch (Exception e) {
            log.error("Error descontando stock", e);
            return false;
        }
    }

    private boolean simularPago(Long pedidoId, BigDecimal total) {
        try {
            Map<String, Object> body = Map.of("pedidoId", pedidoId, "monto", total);
            PagoResponse resp = pagoWebClient.post()
                    .uri("/pago/simular")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(PagoResponse.class)
                    .block();
            return resp != null && resp.isAprobado();
        } catch (Exception e) {
            log.error("Error comunicando con ms-pago", e);
            throw new ExternalServiceException("Error comunicando con ms-pago");
        }
    }

    // Simple mapping entity -> DTO
    private PedidoResponse mapToResponse(Pedido p) {
        List<DetallePedidoResponse> detalles = p.getDetalles().stream().map(d ->
                DetallePedidoResponse.builder()
                        .id(d.getId())
                        .productoId(d.getProductoId())
                        .nombreProducto(d.getNombreProducto())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getSubtotal())
                        .build()
        ).collect(Collectors.toList());

        return PedidoResponse.builder()
                .id(p.getId())
                .usuarioId(p.getUsuarioId())
                .direccionId(p.getDireccionId())
                .estadoPedido(p.getEstadoPedido().name())
                .subtotal(p.getSubtotal())
                .descuento(p.getDescuento())
                .total(p.getTotal())
                .notas(p.getNotas())
                .creadoEn(p.getCreadoEn())
                .actualizadoEn(p.getActualizadoEn())
                .detalles(detalles)
                .build();
    }

    // DTOs internos para llamadas a servicios remotos (pueden definirse como clases estáticas o en otro paquete)
    private static class ProductoInfo {
        private Long id;
        private String nombre;
        private BigDecimal precio;

        // getters y setters (o usar Lombok) — para parsing con WebClient
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public BigDecimal getPrecio() { return precio; }
        public void setPrecio(BigDecimal precio) { this.precio = precio; }
    }

    private static class PagoResponse {
        private boolean aprobado;
        public boolean isAprobado() { return aprobado; }
        public void setAprobado(boolean aprobado) { this.aprobado = aprobado; }
    }
}