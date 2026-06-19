package cl.dsy1103.ms_pedido.service;

import cl.dsy1103.ms_pedido.config.*;
import cl.dsy1103.ms_pedido.dto.*;
import cl.dsy1103.ms_pedido.exception.BadRequestException;
import cl.dsy1103.ms_pedido.exception.NotFoundException;
import cl.dsy1103.ms_pedido.model.*;
import cl.dsy1103.ms_pedido.repository.DetallePedidoRepository;
import cl.dsy1103.ms_pedido.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private CatalogoClient catalogoClient;

    @Mock
    private InventarioClient inventarioClient;

    @Mock
    private PagoClient pagoClient;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    @Test
    void testCrearPedidoExitoso() {

        CrearPedidoRequest request = pedidoService.crearRequestValido();

        CatalogoClient.ProductoInfo producto =
                new CatalogoClient.ProductoInfo();

        producto.setId(20L);
        producto.setNombre("Laptop");
        producto.setPrecio(new BigDecimal("1000"));

        when(catalogoClient.obtenerProducto(20L))
                .thenReturn(producto);

        when(inventarioClient.verificarStock(20L, 2))
                .thenReturn(Map.of(
                        "disponible", true
                ));

        Pedido pedidoGuardado =
                Pedido.builder()
                        .id(1L)
                        .usuarioId(1L)
                        .estadoPedido(EstadoPedido.PENDIENTE)
                        .build();

        when(pedidoRepository.save(any(Pedido.class)))
                .thenReturn(pedidoGuardado);

        when(pagoClient.procesarPago(anyMap()))
                .thenReturn(Map.of(
                        "estado", "APROBADO"
                ));

        when(inventarioClient.verificarStock(20L, 1))
                .thenReturn(Map.of(
                        "id", 100,
                        "disponible", true
                ));

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(
                        Pedido.builder()
                                .id(1L)
                                .usuarioId(1L)
                                .estadoPedido(EstadoPedido.CONFIRMADO)
                                .build()
                ));

        PedidoResponse response =
                pedidoService.crearPedido(request); //error aca

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void testDebeLanzarExcepcionSiProductoNoExiste() {

        CrearPedidoRequest request =
                pedidoService.crearRequestValido();

        when(catalogoClient.obtenerProducto(20L))
                .thenReturn(null);

        assertThrows(
                NotFoundException.class,
                () -> pedidoService.crearPedido(request)
        );
    }

    @Test
    void crearPedidoExitoso() {

        CrearPedidoRequest request = pedidoService.crearRequestValido();

        CatalogoClient.ProductoInfo producto =
                new CatalogoClient.ProductoInfo();

        producto.setId(20L);
        producto.setNombre("Laptop");
        producto.setPrecio(new BigDecimal("1000"));

        when(catalogoClient.obtenerProducto(20L))
                .thenReturn(producto);

        when(inventarioClient.verificarStock(20L, 2))
                .thenReturn(Map.of(
                        "disponible", true
                ));

        Pedido pedidoGuardado =
                Pedido.builder()
                        .id(1L)
                        .usuarioId(1L)
                        .estadoPedido(EstadoPedido.PENDIENTE)
                        .build();

        when(pedidoRepository.save(any(Pedido.class)))
                .thenReturn(pedidoGuardado);

        when(pagoClient.procesarPago(anyMap()))
                .thenReturn(Map.of(
                        "estado", "APROBADO"
                ));

        when(inventarioClient.verificarStock(20L, 1))
                .thenReturn(Map.of(
                        "id", 100,
                        "disponible", true
                ));

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(
                        Pedido.builder()
                                .id(1L)
                                .usuarioId(1L)
                                .estadoPedido(EstadoPedido.CONFIRMADO)
                                .build()
                ));

        PedidoResponse response =
                pedidoService.crearPedido(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void testDebeLanzarExcepcionSiNoHayStock() {

        CrearPedidoRequest request =
                pedidoService.crearRequestValido();

        CatalogoClient.ProductoInfo producto =
                new CatalogoClient.ProductoInfo();

        producto.setId(20L);
        producto.setNombre("Laptop");
        producto.setPrecio(new BigDecimal("1000"));

        when(catalogoClient.obtenerProducto(20L))
                .thenReturn(producto);

        when(inventarioClient.verificarStock(20L, 2))
                .thenReturn(Map.of(
                        "disponible", false
                ));

        assertThrows(
                BadRequestException.class,
                () -> pedidoService.crearPedido(request)
        );
    }

    @Test
    void testDebeProcesarPagoAprobado() {

        CrearPedidoRequest request =
                pedidoService.crearRequestValido();

        CatalogoClient.ProductoInfo producto =
                new CatalogoClient.ProductoInfo();

        producto.setId(20L);
        producto.setNombre("Laptop");
        producto.setPrecio(new BigDecimal("1000"));

        when(catalogoClient.obtenerProducto(20L))
                .thenReturn(producto);

        when(inventarioClient.verificarStock(anyLong(), eq(2)))
                .thenReturn(Map.of(
                        "disponible", true
                ));

        when(inventarioClient.verificarStock(anyLong(), eq(1)))
                .thenReturn(Map.of(
                        "id", 10,
                        "disponible", true
                ));

        when(pedidoRepository.save(any()))
                .thenReturn(
                        Pedido.builder()
                                .id(1L)
                                .build()
                );

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(
                        Pedido.builder()
                                .id(1L)
                                .estadoPedido(EstadoPedido.CONFIRMADO)
                                .build()
                ));

        when(pagoClient.procesarPago(anyMap()))
                .thenReturn(Map.of(
                        "estado", "APROBADO"
                ));

        pedidoService.crearPedido(request);

        verify(pagoClient)
                .procesarPago(anyMap());
    }

    @Test
    void debeCancelarPedidoSiPagoEsRechazado() {

        CrearPedidoRequest request =
                pedidoService.crearRequestValido();

        CatalogoClient.ProductoInfo producto =
                new CatalogoClient.ProductoInfo();

        producto.setId(20L);
        producto.setNombre("Laptop");
        producto.setPrecio(new BigDecimal("1000"));

        when(catalogoClient.obtenerProducto(20L))
                .thenReturn(producto);

        when(inventarioClient.verificarStock(anyLong(), eq(2)))
                .thenReturn(Map.of(
                        "disponible", true
                ));

        when(pedidoRepository.save(any()))
                .thenReturn(
                        Pedido.builder()
                                .id(1L)
                                .build()
                );

        when(pagoClient.procesarPago(anyMap()))
                .thenReturn(Map.of(
                        "estado", "RECHAZADO"
                ));

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(
                        Pedido.builder()
                                .id(1L)
                                .estadoPedido(EstadoPedido.CANCELADO)
                                .build()
                ));

        PedidoResponse response =
                pedidoService.crearPedido(request);

        assertNotNull(response);

        verify(pagoClient)
                .procesarPago(anyMap());
    }
}
