package io.github.vmarins2005.loja;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * O pacote raiz nao e um modulo: e o lugar compartilhado onde ficam as pecas que todos os
 * modulos usam. Manter isso pequeno e parte da disciplina - o pacote raiz e por onde uma
 * arquitetura modular costuma comecar a vazar.
 */
@Configuration
@EnableAsync
class ConfiguracaoDaAplicacao {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }
}
