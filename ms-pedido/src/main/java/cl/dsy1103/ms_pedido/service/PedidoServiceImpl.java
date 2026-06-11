package cl.dsy1103.ms_pedido.service;

import cl.dsy1103.ms_pedido.dto.*;
import cl.dsy1103.ms_pedido.exception.*;
import cl.dsy1103.ms_pedido.model.*;
import cl.dsy1103.ms_pedido.repository.*;


import cl.dsy1103.ms_pedido.dto.*;
import cl.dsy1103.ms_pedido.exception.*;
import cl.dsy1103.ms_pedido.model.*;
import cl.dsy1103.ms_pedido.repository.*;
import cl.dsy1103.ms_pedido.config.*;

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

    // Clientes balanceados (patrón estándar de FrikiTienda)
    private final UsuarioClient usuarioClient;
    private final CatalogoClient catalogoClient;
    private final InventarioClient inventarioClient;
    private final PagoClient pagoClient;

    @Override
    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PedidoResponse obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado: " + id));
    }

    @Override
    public List<PedidoResponse> obtenerPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Flujo principal de creación del pedido:
     * 1. Validar que el usuario existe (ms-usuarios)
     * 2. Validar productos, obtener nombres/precios y verificar stock (ms-catalogo, ms-inventario)
     * 3. Crear el pedido en estado PENDIENTE
     * 4. Simular el pago (ms-pago)
     * 5. Si se aprueba → descontar stock y confirmar pedido
     * 6. Si se rechaza → cancelar pedido
     */


    @Override
    public PedidoResponse crearPedido(CrearPedidoRequest request) {
        Long direccionId = request.getDireccionId() != null ? request.getDireccionId() : 0L;

        // 1) Validar usuario
        if (!usuarioClient.existeUsuario(request.getUsuarioId())) {
            throw new NotFoundException("Usuario no encontrado: " + request.getUsuarioId());
        }

        // 2) Validar productos, obtener precios y verificar stock
        List<DetallePedidoRequest> detallesReq = request.getDetalles();
        Map<Long, CatalogoClient.ProductoInfo> infoProductos = new HashMap<>();

        for (DetallePedidoRequest detReq : detallesReq) {
            var info = catalogoClient.obtenerProducto(detReq.getProductoId());
            if (info == null) {
                throw new NotFoundException("Producto no encontrado: " + detReq.getProductoId());
            }
            if (!inventarioClient.verificarStock(detReq.getProductoId(), detReq.getCantidad())) {
                throw new BadRequestException("Stock insuficiente para producto: " + detReq.getProductoId());
            }
            infoProductos.put(detReq.getProductoId(), info);
        }

        // 3) Construir pedido (estado PENDIENTE)
        //
        Pedido pedido = Pedido.builder()
                .usuarioId(request.getUsuarioId())
                .direccionId(direccionId) //tiene un valor por defecto ya que deberia implementarse con otro ms-
                .estadoPedido(EstadoPedido.PENDIENTE)
                .descuento(request.getDescuento() != null ? request.getDescuento() : BigDecimal.ZERO)
                .notas(request.getNotas())
                .creadoEn(OffsetDateTime.now())
                .actualizadoEn(OffsetDateTime.now())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetallePedidoRequest detReq : detallesReq) {
            var info = infoProductos.get(detReq.getProductoId());
            BigDecimal precio = info.getPrecio();
            BigDecimal detSubtotal = precio.multiply(BigDecimal.valueOf(detReq.getCantidad()));
            subtotal = subtotal.add(detSubtotal);

            DetallePedido detalle = DetallePedido.builder()
                    .pedido(pedido)
                    .productoId(detReq.getProductoId())
                    .nombreProducto(info.getNombre())
                    .cantidad(detReq.getCantidad())
                    .precioUnitario(precio)
                    .subtotal(detSubtotal)
                    .build();
            pedido.getDetalles().add(detalle);
        }
        pedido.setSubtotal(subtotal);
        BigDecimal total = subtotal.subtract(pedido.getDescuento());
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;
        pedido.setTotal(total);

        // 4) Guardar pedido (transacción corta)
        pedido = savePedidoShortTx(pedido);

        // 5) Simular pago
        boolean pagoAprobado = pagoClient.procesarPago(pedido.getId(), pedido.getTotal());

        if (pagoAprobado) {
            // 6) Descontar stock
            for (DetallePedidoRequest detReq : detallesReq) {
                inventarioClient.descontarStock(
                        detReq.getProductoId(),
                        detReq.getCantidad(),
                        "Pedido #" + pedido.getId()
                );
            }
            updateEstadoPedido(pedido.getId(), EstadoPedido.CONFIRMADO);
        } else {
            updateEstadoPedido(pedido.getId(), EstadoPedido.CANCELADO);
        }

        return obtenerPorId(pedido.getId());
    }

    @Transactional
    protected Pedido savePedidoShortTx(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    @Transactional
    protected Pedido updateEstadoPedido(Long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado: " + pedidoId));
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

    // ---------- Mapeo a DTO ----------
    private PedidoResponse mapToResponse(Pedido p) {
        List<DetallePedidoResponse> detalles = p.getDetalles().stream()
                .map(d -> DetallePedidoResponse.builder()
                        .id(d.getId())
                        .productoId(d.getProductoId())
                        .nombreProducto(d.getNombreProducto())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getSubtotal())
                        .build())
                .collect(Collectors.toList());

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

    @Override
    @Transactional(readOnly = true)
    public boolean existeCompra(Long usuarioId, Long productoId) {
        return pedidoRepository.findByUsuarioId(usuarioId)
                .stream()
                .filter(p -> p.getEstadoPedido() == EstadoPedido.CONFIRMADO
                        || p.getEstadoPedido() == EstadoPedido.ENTREGADO)
                .flatMap(p -> p.getDetalles().stream())
                .anyMatch(d -> d.getProductoId().equals(productoId));
    }
    }