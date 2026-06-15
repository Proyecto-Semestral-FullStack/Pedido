package cl.dsy1103.ms_pedido;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class MsPedidoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsPedidoApplication.class, args);
	}

}
