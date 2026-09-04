# ADR 0001 — Monólito modular em vez de microserviços

Status: aceito · 2026-09-04 · supera: —

## Contexto

Catálogo, pedidos e faturamento são três contextos com vocabulários próprios. A pergunta
não é se eles devem ser separados — devem — mas **em que nível**: pacotes, módulos do
mesmo deploy, ou serviços com processo e banco próprios.

A resposta depende de dois fatores que quase nunca aparecem na discussão:

1. **Quão certa é a fronteira?** Fronteira errada em microserviço custa migração de dados,
   contrato entre times e coordenação de deploy. Fronteira errada em módulo custa uma
   tarde de refatoração.
2. **Qual o gargalo real?** Escalar independentemente só importa quando um contexto tem
   perfil de carga muito diferente dos outros.

## Alternativas

**1. Pacotes, sem fronteira verificada.**
Zero cerimônia. E zero garantia: a fronteira dura até a primeira sexta-feira apertada. Foi
assim que a maioria dos monólitos "organizados por domínio" virou monólito comum.

**2. Três serviços, com banco próprio cada um.**
Escala e deploy independentes de verdade. Custo alto e imediato:
- toda chamada entre contextos vira rede, com timeout, retry e falha parcial;
- a consulta ao catálogo na hora de adicionar item vira chamada HTTP que pode falhar;
- transação que hoje é local vira saga com compensação;
- três pipelines, três ambientes, observabilidade distribuída para responder "onde está
  lento".

Nada disso é impossível — é o assunto de outros projetos da série. Mas é custo pago
**antes** de a fronteira estar provada.

**3. Monólito modular com fronteira verificada pelo build. — escolhida**

## Decisão

Um deploy, três módulos, fronteira verificada por `ApplicationModules.verify()` no CI.

O ponto central: **este desenho não impede a migração para serviços; ele a prepara.**
Cada módulo tem schema próprio, não faz join com os outros e comunica-se por evento ou
pela API pública do vizinho. Extrair `faturamento` para serviço próprio significa trocar
o listener local por um consumidor de mensageria — o modelo de dados já está separado, e
não há FK para desfazer.

O sinal objetivo de que chegou a hora de separar: um contexto precisa escalar
diferente dos outros, ou dois times passam a disputar o mesmo deploy. Nenhum dos dois é o
caso de um sistema que ainda não existe.

## Consequências

- \+ Refatorar a fronteira custa uma tarde, e não uma migração.
- \+ Uma transação, um banco, um pipeline. A conta de complexidade só é paga quando houver
  motivo.
- \+ A separação é real: schema por contexto, sem join, sem FK cruzada.
- − Escala é conjunta. Se o catálogo tomar dez vezes mais tráfego, a aplicação inteira
  escala junto.
- − Falha em um módulo pode derrubar o processo inteiro. Não há isolamento de processo.
- − A fronteira é verificada por teste, e não pelo compilador: um import proibido compila.
  Só é uma garantia real porque o teste roda no CI e bloqueia merge — sem isso, esta
  decisão vira a alternativa 1 disfarçada.
- − "Monólito" ainda soa a coisa velha em entrevista e em reunião de arquitetura. O
  argumento a favor precisa ser feito com os dois fatores do contexto, e não com gosto
  pessoal.
