package cl.dsy1103.ms_pedido.controller;

import cl.dsy1103.ms_pedido.dto.*;
import cl.dsy1103.ms_pedido.model.DetallePedido;
import cl.dsy1103.ms_pedido.model.EstadoPedido;
import cl.dsy1103.ms_pedido.model.Pedido;
import cl.dsy1103.ms_pedido.service.PedidoServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoController.class)
public class PedidoControllerTest{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PedidoServiceImpl pedidoService;

    @Test
    void testObtenerTodosLosPedidos() throws JsonProcessingException {
        OffsetDateTime ahora =OffsetDateTime.now();
        DetallePedidoResponse detalle1 = new DetallePedidoResponse();
        detalle1.setId(501L);
        detalle1.setProductoId(20L);
        detalle1.setNombreProducto("Laptop Gamer Asus");
        detalle1.setCantidad(1);
        detalle1.setPrecioUnitario(new BigDecimal("1200.00"));
        detalle1.setSubtotal(new BigDecimal("1200.00"));

        DetallePedidoResponse detalle2 = new DetallePedidoResponse();
        detalle2.setId(502L);
        detalle2.setProductoId(35L);
        detalle2.setNombreProducto("Mouse Óptico Inalámbrico");
        detalle2.setCantidad(2);
        detalle2.setPrecioUnitario(new BigDecimal("25.00"));
        detalle2.setSubtotal(new BigDecimal("50.00"));

        DetallePedidoResponse detalle3 = new DetallePedidoResponse();
        detalle3.setId(503L);
        detalle3.setProductoId(50L);
        detalle3.setNombreProducto("Teclado Mecánico RGB");
        detalle3.setCantidad(1);
        detalle3.setPrecioUnitario(new BigDecimal("80.00"));
        detalle3.setSubtotal(new BigDecimal("80.00"));

        DetallePedidoResponse detalle4 = new DetallePedidoResponse();
        detalle4.setId(504L);
        detalle4.setProductoId(65L);
        detalle4.setNombreProducto("Monitor Curvo 27\"");
        detalle4.setCantidad(1);
        detalle4.setPrecioUnitario(new BigDecimal("300.00"));
        detalle4.setSubtotal(new BigDecimal("300.00"));

        List<DetallePedidoResponse> listaDetalles1 = new ArrayList<>();
        listaDetalles1.add(detalle1);
        listaDetalles1.add(detalle2);

        List<DetallePedidoResponse> listaDetalles2 = new ArrayList<>();
        listaDetalles2.add(detalle3);
        listaDetalles2.add(detalle4);

        PedidoResponse pedido1 = new PedidoResponse();
        pedido1.setId(1L);
        pedido1.setUsuarioId(45L);
        pedido1.setDireccionId(12L);
        pedido1.setEstadoPedido(EstadoPedido.PENDIENTE);
        pedido1.setSubtotal(new BigDecimal("1250.00"));
        pedido1.setDescuento(new BigDecimal("50.00"));
        pedido1.setTotal(new BigDecimal("1200.00"));
        pedido1.setNotas("Entregar envuelto para regalo.");
        pedido1.setCreadoEn(OffsetDateTime.now());
        pedido1.setActualizadoEn(OffsetDateTime.now());

        PedidoResponse pedido2 = new PedidoResponse();
        pedido2.setId(2L);
        pedido2.setUsuarioId(50L);
        pedido2.setDireccionId(15L);
        pedido2.setEstadoPedido(EstadoPedido.ENVIADO);
        pedido2.setSubtotal(new BigDecimal("380.00"));
        pedido2.setDescuento(new BigDecimal("30.00"));
        pedido2.setTotal(new BigDecimal("350.00"));
        pedido2.setNotas("Llamar al llegar.");
        pedido2.setCreadoEn(OffsetDateTime.now());
        pedido2.setActualizadoEn(OffsetDateTime.now());

        pedido1.setDetalles(listaDetalles1);
        pedido2.setDetalles(listaDetalles2);
        List<PedidoResponse> listaPedidos = List.of(pedido1, pedido2);

        when(pedidoService.listarTodos()).thenReturn(listaPedidos);
            try {
                mockMvc.perform(get("/api/pedidos"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.length()").value(2));
            }catch(Exception e){
                fail("La petición MockMvc de obtener todos falló debido a: " + e.getMessage(), e);
            }
    }

    @Test
    void testObtenerPorId(){
        Long id = 1L;
        OffsetDateTime ahora =OffsetDateTime.now();
        DetallePedidoResponse detalle1 = new DetallePedidoResponse();
        detalle1.setId(501L);
        detalle1.setProductoId(20L);
        detalle1.setNombreProducto("Laptop Gamer Asus");
        detalle1.setCantidad(1);
        detalle1.setPrecioUnitario(new BigDecimal("1200.00"));
        detalle1.setSubtotal(new BigDecimal("1200.00"));

        DetallePedidoResponse detalle2 = new DetallePedidoResponse();
        detalle2.setId(502L);
        detalle2.setProductoId(35L);
        detalle2.setNombreProducto("Mouse Óptico Inalámbrico");
        detalle2.setCantidad(2);
        detalle2.setPrecioUnitario(new BigDecimal("25.00"));
        detalle2.setSubtotal(new BigDecimal("50.00"));

        List<DetallePedidoResponse> listaDetalles = new ArrayList<>();
        listaDetalles.add(detalle1);
        listaDetalles.add(detalle2);

        PedidoResponse pedido = new PedidoResponse();
        pedido.setId(1L);
        pedido.setUsuarioId(45L);
        pedido.setDireccionId(12L);
        pedido.setEstadoPedido(EstadoPedido.PENDIENTE);
        pedido.setSubtotal(new BigDecimal("1250.00"));
        pedido.setDescuento(new BigDecimal("50.00"));
        pedido.setTotal(new BigDecimal("1200.00"));
        pedido.setNotas("Entregar envuelto para regalo.");
        pedido.setCreadoEn(OffsetDateTime.now());
        pedido.setActualizadoEn(OffsetDateTime.now());
        pedido.setDetalles(listaDetalles);

        when(pedidoService.obtenerPorId(id)).thenReturn(pedido);
        try{
            mockMvc.perform(get("/api/pedidos/{id}",id))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.usuarioId").value(45));
        }catch(Exception e){
            fail("La petición MockMvc falló debido a: " + e.getMessage(), e);
        }
    }

    @Test
    void testObtenerPorUsuario(){
        OffsetDateTime ahora =OffsetDateTime.now();
        DetallePedidoResponse detalle1 = new DetallePedidoResponse();
        detalle1.setId(501L);
        detalle1.setProductoId(20L);
        detalle1.setNombreProducto("Laptop Gamer Asus");
        detalle1.setCantidad(1);
        detalle1.setPrecioUnitario(new BigDecimal("1200.00"));
        detalle1.setSubtotal(new BigDecimal("1200.00"));

        DetallePedidoResponse detalle2 = new DetallePedidoResponse();
        detalle2.setId(502L);
        detalle2.setProductoId(35L);
        detalle2.setNombreProducto("Mouse Óptico Inalámbrico");
        detalle2.setCantidad(2);
        detalle2.setPrecioUnitario(new BigDecimal("25.00"));
        detalle2.setSubtotal(new BigDecimal("50.00"));

        DetallePedidoResponse detalle3 = new DetallePedidoResponse();
        detalle3.setId(503L);
        detalle3.setProductoId(50L);
        detalle3.setNombreProducto("Teclado Mecánico RGB");
        detalle3.setCantidad(1);
        detalle3.setPrecioUnitario(new BigDecimal("80.00"));
        detalle3.setSubtotal(new BigDecimal("80.00"));

        DetallePedidoResponse detalle4 = new DetallePedidoResponse();
        detalle4.setId(504L);
        detalle4.setProductoId(65L);
        detalle4.setNombreProducto("Monitor Curvo 27\"");
        detalle4.setCantidad(1);
        detalle4.setPrecioUnitario(new BigDecimal("300.00"));
        detalle4.setSubtotal(new BigDecimal("300.00"));

        List<DetallePedidoResponse> listaDetalles1 = new ArrayList<>();
        listaDetalles1.add(detalle1);
        listaDetalles1.add(detalle2);

        List<DetallePedidoResponse> listaDetalles2 = new ArrayList<>();
        listaDetalles2.add(detalle3);
        listaDetalles2.add(detalle4);

        PedidoResponse pedido1 = new PedidoResponse();
        pedido1.setId(1L);
        pedido1.setUsuarioId(45L);
        pedido1.setDireccionId(12L);
        pedido1.setEstadoPedido(EstadoPedido.PENDIENTE);
        pedido1.setSubtotal(new BigDecimal("1250.00"));
        pedido1.setDescuento(new BigDecimal("50.00"));
        pedido1.setTotal(new BigDecimal("1200.00"));
        pedido1.setNotas("Entregar envuelto para regalo.");
        pedido1.setCreadoEn(OffsetDateTime.now());
        pedido1.setActualizadoEn(OffsetDateTime.now());

        PedidoResponse pedido2 = new PedidoResponse();
        pedido2.setId(2L);
        pedido2.setUsuarioId(50L);
        pedido2.setDireccionId(15L);
        pedido2.setEstadoPedido(EstadoPedido.ENVIADO);
        pedido2.setSubtotal(new BigDecimal("380.00"));
        pedido2.setDescuento(new BigDecimal("30.00"));
        pedido2.setTotal(new BigDecimal("350.00"));
        pedido2.setNotas("Llamar al llegar.");
        pedido2.setCreadoEn(OffsetDateTime.now());
        pedido2.setActualizadoEn(OffsetDateTime.now());

        pedido1.setDetalles(listaDetalles1);
        pedido2.setDetalles(listaDetalles2);

        List<PedidoResponse> listaPedidos = List.of(pedido1, pedido2);

        when(pedidoService.obtenerPorUsuario(45L)).thenReturn(List.of(pedido1));
        try{
            mockMvc.perform(get("/api/pedidos/usuario/{usuarioId}",45L))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].usuarioId").value(45L));
        }catch(Exception e){
            fail("La petición MockMvc falló debido a: " + e.getMessage(), e);
        }
    }

    @Test
    void testCrearPedido() {

        OffsetDateTime ahora = OffsetDateTime.now();

        DetallePedidoRequest detalleRequest = new DetallePedidoRequest();
        detalleRequest.setProductoId(20L);
        detalleRequest.setCantidad(2);

        CrearPedidoRequest request = new CrearPedidoRequest();
        request.setUsuarioId(45L);
        request.setDireccionId(12L);
        request.setDescuento(new BigDecimal("50.00"));
        request.setNotas("Entregar envuelto para regalo.");
        request.setDetalles(List.of(detalleRequest));

        DetallePedidoResponse detalleResponse = new DetallePedidoResponse();
        detalleResponse.setId(501L);
        detalleResponse.setProductoId(20L);
        detalleResponse.setNombreProducto("Laptop Gamer Asus");
        detalleResponse.setCantidad(2);
        detalleResponse.setPrecioUnitario(new BigDecimal("1200.00"));
        detalleResponse.setSubtotal(new BigDecimal("2400.00"));

        PedidoResponse response = new PedidoResponse();
        response.setId(1L);
        response.setUsuarioId(45L);
        response.setDireccionId(12L);
        response.setEstadoPedido(EstadoPedido.CONFIRMADO);
        response.setSubtotal(new BigDecimal("2400.00"));
        response.setDescuento(new BigDecimal("50.00"));
        response.setTotal(new BigDecimal("2350.00"));
        response.setNotas("Entregar envuelto para regalo.");
        response.setCreadoEn(ahora);
        response.setActualizadoEn(ahora);
        response.setDetalles(List.of(detalleResponse));

        when(pedidoService.crearPedido(any(CrearPedidoRequest.class)))
                .thenReturn(response);

        try {

            mockMvc.perform(
                            post("/api/pedidos")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON))

                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.usuarioId").value(45))
                    .andExpect(jsonPath("$.direccionId").value(12))
                    .andExpect(jsonPath("$.estadoPedido")
                            .value("CONFIRMADO"))
                    .andExpect(jsonPath("$.subtotal")
                            .value(2400.00))
                    .andExpect(jsonPath("$.descuento")
                            .value(50.00))
                    .andExpect(jsonPath("$.total")
                            .value(2350.00))
                    .andExpect(jsonPath("$.notas")
                            .value("Entregar envuelto para regalo."))

                    .andExpect(jsonPath("$.creadoEn").exists())
                    .andExpect(jsonPath("$.actualizadoEn").exists())

                    .andExpect(jsonPath("$.detalles.length()")
                            .value(1))
                    .andExpect(jsonPath("$.detalles[0].id")
                            .value(501))
                    .andExpect(jsonPath("$.detalles[0].productoId")
                            .value(20))
                    .andExpect(jsonPath("$.detalles[0].nombreProducto")
                            .value("Laptop Gamer Asus"))
                    .andExpect(jsonPath("$.detalles[0].cantidad")
                            .value(2))
                    .andExpect(jsonPath("$.detalles[0].precioUnitario")
                            .value(1200.00))
                    .andExpect(jsonPath("$.detalles[0].subtotal")
                            .value(2400.00));

        } catch (Exception e) {
            fail("La petición MockMvc de crear pedido falló debido a: "
                    + e.getMessage(), e);
        }
    }

    @Test
    void testActualizarEstado() {
        Long id = 1L;
        CambiarEstadoPedidoRequest estadoRequest = new CambiarEstadoPedidoRequest();
        estadoRequest.setEstadoPedido(EstadoPedido.CONFIRMADO);
        Pedido pedidoRequest = new Pedido();
        pedidoRequest.setId(id);
        pedidoRequest.setUsuarioId(45L);
        pedidoRequest.setEstadoPedido(estadoRequest.getEstadoPedido());
        pedidoRequest.setSubtotal(new BigDecimal("1250.00"));
        pedidoRequest.setDescuento(new BigDecimal("50.00"));
        pedidoRequest.setTotal(new BigDecimal("1200.00"));
        pedidoRequest.setNotas("Entregar envuelto para regalo.");

        DetallePedido detallePedido1 = new DetallePedido();
        detallePedido1.setId(501L);
        detallePedido1.setPedido(pedidoRequest);
        detallePedido1.setProductoId(20L);
        detallePedido1.setNombreProducto("Laptop Gamer Asus");
        detallePedido1.setCantidad(1);
        detallePedido1.setPrecioUnitario(new BigDecimal("1200.00"));
        detallePedido1.setSubtotal(new BigDecimal("1200.00"));
        detallePedido1.setPedido(pedidoRequest);

        DetallePedido detallePedido2 = new DetallePedido();
        detallePedido2.setId(502L);
        detallePedido2.setPedido(pedidoRequest);
        detallePedido2.setProductoId(35L);
        detallePedido2.setNombreProducto("Mouse Óptico Inalámbrico");
        detallePedido2.setCantidad(2);
        detallePedido2.setPrecioUnitario(new BigDecimal("25.00"));
        detallePedido2.setSubtotal(new BigDecimal("50.00"));
        detallePedido2.setPedido(pedidoRequest);

        pedidoRequest.setDetalles(List.of(detallePedido1, detallePedido2));
        when(pedidoService.cambiarEstado(eq(id),any(CambiarEstadoPedidoRequest.class))).thenReturn(new PedidoResponse());
        try{
            mockMvc.perform(put("/api/pedidos/{id}/estado",id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(estadoRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            fail("La petición MockMvc de actualizar estado falló debido a: " + e.getMessage(), e);
        }
    }

    @Test
    void testBorrarPedido(){
        Long id = 1L;
        try{
            mockMvc.perform(delete("/api/pedidos/{id}",id))
                    .andExpect(status().isNoContent());
        }catch(Exception e){
            fail("La petición MockMvc de eliminar pedido falló debido a: " + e.getMessage(), e);
        }
    }

    @Test
    void testHealth() {
        when(pedidoService.health()).thenReturn("OK");
        try {
            mockMvc.perform(get("/api/pedidos/health"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("OK"));
        } catch (Exception e) {
            fail("La petición MockMvc de health falló debido a: " + e.getMessage(), e);
        }
    }
}
