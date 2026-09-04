package io.github.vmarins2005.loja;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * O teste que sustenta o projeto inteiro.
 *
 * <p>{@code verify()} falha quando um modulo referencia tipo interno de outro, quando
 * depende de modulo que nao declarou em {@code allowedDependencies}, ou quando dois
 * modulos criam dependencia circular.
 *
 * <p>Sem isto, "monolito modular" e so um jeito de organizar pastas: a primeira sexta-feira
 * apertada resolve o problema com um import, e ninguem percebe ate a arquitetura ter
 * virado um monolito comum de novo.
 *
 * <p>Para ver a falha acontecendo, abra
 * {@code faturamento.internal.EmissorDeFaturas} e injete {@code Pedidos} no construtor.
 * O modulo declara depender apenas de {@code pedidos :: eventos}, e este teste reprova.
 */
class EstruturaDosModulosTest {

    private final ApplicationModules modulos = ApplicationModules.of(Aplicacao.class);

    @Test
    @DisplayName("as fronteiras declaradas entre os modulos sao respeitadas")
    void fronteirasRespeitadas() {
        modulos.verify();
    }

    @Test
    @DisplayName("os tres contextos existem e estao nomeados")
    void tresModulos() {
        assertThat(modulos.stream().map(modulo -> modulo.getName()))
                .containsExactlyInAnyOrder("catalogo", "pedidos", "faturamento");
    }

    @Test
    @DisplayName("imprime a estrutura - util para colar na descricao de um PR de arquitetura")
    void imprimeEstrutura() {
        modulos.forEach(modulo -> System.out.println(modulo.toString()));
    }
}
