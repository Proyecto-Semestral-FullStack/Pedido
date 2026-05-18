package cl.dsy1103.ms_pedido.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}