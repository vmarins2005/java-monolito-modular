package io.github.vmarins2005.loja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;

/**
 * Raiz da aplicacao e raiz dos modulos.
 *
 * <p>Cada subpacote direto daqui - {@code catalogo}, {@code pedidos}, {@code faturamento} -
 * e um modulo de aplicacao. Os tipos no pacote raiz de um modulo sao a API dele; tudo em
 * subpacote e interno e invisivel para os outros, com excecao das interfaces nomeadas
 * declaradas explicitamente.
 *
 * <p>Isso nao e convencao documentada: {@code EstruturaDosModulosTest} quebra o build
 * quando alguem atravessa a fronteira.
 */
@Modulith
@SpringBootApplication
public class Aplicacao {

    public static void main(String[] argumentos) {
        SpringApplication.run(Aplicacao.class, argumentos);
    }
}
