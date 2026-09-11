# Challenge Clyvo - Olli Pet (API)

API REST em Spring Boot que atende o aplicativo mobile da clinica veterinaria Olli Pet.
Toda a superficie publica esta em `/api/v1/**`.

---

## Divisao de responsabilidades com o Firebase

O aplicativo mobile usa Firebase e continua usando. O que muda e onde cada coisa mora:

| Responsabilidade | Onde fica | Por que |
|---|---|---|
| Login e cadastro de usuario | **Firebase Authentication** | ja implementado no app e exigido na disciplina de mobile |
| Foto do pet, notificacao push | **Firebase** (Storage + FCM) | a API nao faria melhor |
| Consultas, vacinacao, prontuarios | **Esta API + H2/Flyway** | exigem regra de negocio que o Firestore nao aplica |

O Firestore grava o que mandarem. Quem impede duas consultas no mesmo horario do mesmo
veterinario, quem calcula a proxima dose da V10 e quem so deixa concluir uma consulta que
estava em atendimento e o backend — e essa e a razao de ele existir aqui.

### Como o app autentica

O app **nao faz um segundo login**. Ele envia o ID token que o Firebase ja devolve:

```js
const token = await auth.currentUser.getIdToken();

await fetch("http://localhost:8080/api/v1/consultas", {
  method: "POST",
  headers: {
    "Authorization": `Bearer ${token}`,
    "Content-Type": "application/json",
  },
  body: JSON.stringify({ petId, veterinarioId, dataHora, motivo }),
});
```

A API valida esse token contra as chaves publicas do Google. Basta configurar o ID do
projeto Firebase:

```properties
app.security.firebase.project-id=seu-projeto-firebase
```

Sem essa propriedade a aplicacao sobe normalmente e aceita apenas os tokens que ela
mesma emite — util para rodar e apresentar offline.

**Primeiro acesso do app:** logo apos o cadastro no Firebase, chame uma vez
`POST /api/v1/auth/registrar` com o token no header e o CPF no corpo. Isso cria o
cadastro local ligado ao `firebase_uid`. A chamada e idempotente, entao pode ser feita
apos todo login sem efeito colateral.

---

## Perfis e protecao de rotas

| Perfil | Pode |
|---|---|
| `ADMIN` | cadastrar e remover veterinarios, ver a equipe e os tutores |
| `VETERINARIO` | ver a agenda e a fila de triagens graves, confirmar/iniciar/concluir consultas, aplicar vacinas, prescrever tratamentos e acompanhar a adesao, ver todos os pets |
| `RESPONSAVEL` | ver **apenas os proprios pets**, fazer triagem, solicitar e cancelar consultas, consultar a carteira, confirmar as doses do tratamento |

A protecao acontece em tres camadas:

1. **Por URL** (`SecurityConfig`) — `/api/v1/agenda/**`, `/api/v1/triagem/fila` e
   `/api/v1/tratamentos/aderencia-baixa` exigem `VETERINARIO`;
2. **Por metodo** (`@PreAuthorize`) — cada endpoint declara o perfil que aceita;
3. **Por dono** (`PetService.buscarEntidadePermitida`) — um tutor que troque o id na URL
   recebe 403, porque a checagem e de posse do registro e nao so de perfil.

### Como cada perfil entra

O tutor se cadastra sozinho: `POST /api/v1/auth/registrar` cria o cadastro a partir
da conta do Firebase, bastando informar o CPF.

**Veterinario nao se autocadastra.** Se a tela fosse publica, qualquer pessoa que
baixasse o aplicativo poderia se declarar medica e abrir o prontuario de todos os
pacientes. Entao a administracao cadastra a equipe em `POST /api/v1/veterinarios`, e
o profissional apenas define a senha depois. O aplicativo confere em
`GET /api/v1/veterinarios/e-da-equipe?email=` — a unica rota publica alem do login —
se aquele e-mail pertence a clinica antes de criar a conta no Firebase.

Quando um e-mail ja cadastrado aparece em `/auth/registrar`, a API grava o
`firebase_uid` no cadastro existente em vez de criar um tutor. E assim que o
veterinario e a administracao passam a ser reconhecidos pelos proprios perfis.

Remover um veterinario **desativa** o cadastro em vez de apaga-lo: as consultas e os
prontuarios que ele assinou continuam no historico, porque o registro clinico nao
pode perder quem assinou o atendimento.

| Metodo | Rota | Perfil |
|---|---|---|
| POST | `/api/v1/auth/registrar` | qualquer conta do Firebase |
| GET | `/api/v1/veterinarios/e-da-equipe?email=` | publica |
| POST | `/api/v1/veterinarios` | ADMIN |
| DELETE | `/api/v1/veterinarios/{id}` | ADMIN |

---

## Fluxo 1 — Triagem por questionario

O tutor escolhe uma queixa, responde um protocolo fechado e a API devolve uma
classificacao de urgencia. Nada de servico externo: o algoritmo e daqui.

1. **Soma dos pesos** de cada resposta (os pesos nunca sao expostos ao aplicativo);
2. **Sinais de alerta** — uma unica resposta marcada leva direto a `EMERGENCIA`,
   ignorando a pontuacao. Descrevem quadros que matam em horas: torcao gastrica,
   cianose, gato respirando de boca aberta, ingestao de veneno;
3. **Modificadores do cadastro** — e aqui que a API faz o que o aplicativo nao faria.

| Condicao | Ajuste |
|---|---|
| Pet com menos de 6 meses | +3 |
| Pet com 8 anos ou mais | +2 |
| Queixa gastrointestinal **+ vacinacao atrasada** | +5 |
| Gato sem comer ha 3 dias ou mais | +4 |
| Consulta concluida nos ultimos 15 dias | vira `AGENDAR_RETORNO` |

O efeito pratico, com **as mesmas sete respostas**:

| Pet | Vacinacao | Resultado |
|---|---|---|
| Thor | em dia | `ORIENTACAO` — acompanhar em casa |
| Nina | antirrabica atrasada | `POUCO_URGENTE` — agendar |

A justificativa acompanha a resposta: *"Sintoma gastrointestinal com vacinacao em
atraso (Antirrabica atrasada ha 233 dias)"*.

Quando o resultado indica consulta, o aplicativo chama o agendamento passando
`triagemId`. As duas coisas acontecem na mesma transacao, e a consulta nasce com o
relato do tutor anexado.

| Metodo | Rota | Perfil |
|---|---|---|
| GET | `/api/v1/triagem/queixas/pet/{petId}` | RESPONSAVEL |
| GET | `/api/v1/triagem/protocolos/{queixaId}` | ambos |
| POST | `/api/v1/triagem` | RESPONSAVEL |
| GET | `/api/v1/triagem/minhas` | RESPONSAVEL |
| PUT | `/api/v1/triagem/{id}` | RESPONSAVEL |
| DELETE | `/api/v1/triagem/{id}` | RESPONSAVEL |
| GET | `/api/v1/triagem/fila` | VETERINARIO |

O `PUT` reavalia a triagem do zero com novas respostas — serve para quando o tutor
percebe que respondeu errado ou o quadro mudou. Nem ele nem o `DELETE` se aplicam a
triagem que ja originou uma consulta: naquele ponto ela e a justificativa clinica do
agendamento.

Conteudo dos protocolos e contrato detalhado: `docs/protocolos-triagem.md`.

> A triagem e ferramenta de **orientacao, nunca de diagnostico**. Toda resposta carrega
> esse aviso, e `EMERGENCIA` manda ir a clinica sem passar por agendamento.

## Fluxo 2 — Agendamento de consulta

```
SOLICITADA --confirmar--> CONFIRMADA --iniciar--> EM_ATENDIMENTO --concluir--> CONCLUIDA
     |                        |                                                    |
     +--------cancelar--------+------registrarFalta--> NAO_COMPARECEU        gera prontuario
```

O grafo de transicoes vive no enum `StatusConsulta`, e `Consulta.moverPara` e o unico
ponto que muda estado — nao ha como pular de SOLICITADA direto para CONCLUIDA.

Regras aplicadas (`PoliticaDeAgendamento`):

- nao agenda no passado nem aos domingos;
- respeita o expediente da clinica (08:00 as 18:00);
- recusa horario que colida com outro atendimento do mesmo veterinario;
- so o dono do pet solicita;
- tutor cancela consulta confirmada com no minimo 6 horas de antecedencia;
- concluir grava o prontuario na mesma transacao.

| Metodo | Rota | Perfil |
|---|---|---|
| POST | `/api/v1/consultas` | RESPONSAVEL |
| GET | `/api/v1/consultas/minhas` | RESPONSAVEL |
| PATCH | `/api/v1/consultas/{id}/confirmar` | VETERINARIO |
| PATCH | `/api/v1/consultas/{id}/iniciar` | VETERINARIO |
| PATCH | `/api/v1/consultas/{id}/concluir` | VETERINARIO |
| PATCH | `/api/v1/consultas/{id}/falta` | VETERINARIO |
| PATCH | `/api/v1/consultas/{id}/cancelar` | ambos |
| GET | `/api/v1/agenda?data=` | VETERINARIO |
| GET | `/api/v1/agenda/pendentes` | VETERINARIO |

## Fluxo 3 — Carteira de vacinacao

Cada vacina do catalogo carrega o proprio protocolo (numero de doses, intervalo entre
elas e periodicidade do reforco). Ao registrar uma dose, a API calcula sozinha o numero
da dose e o vencimento da proxima — o cliente nao envia nenhum dos dois.

Exemplo real (V10, protocolo de 3 doses a cada 21 dias e reforco anual):

| Dose aplicada em | Proxima dose calculada |
|---|---|
| 10/03/2026 (1a) | 31/03/2026 |
| 31/03/2026 (2a) | 21/04/2026 |
| 21/04/2026 (3a) | **21/04/2027** (serie concluida, virou reforco) |

Validacoes: especie incompativel, protocolo ja concluido, intervalo minimo nao cumprido e
data anterior a ultima dose sao todas recusadas com 409.

| Metodo | Rota | Perfil |
|---|---|---|
| POST | `/api/v1/vacinacao/pets/{petId}/doses` | VETERINARIO |
| GET | `/api/v1/vacinacao/pets/{petId}/carteira` | ambos (dono ou clinica) |
| GET | `/api/v1/vacinacao/pets/{petId}/doses` | ambos (dono ou clinica) |
| GET | `/api/v1/vacinacao/pendencias` | VETERINARIO |
| GET | `/api/v1/vacinacao/catalogo/{especie}` | ambos |

`/vacinacao/pendencias` devolve as doses vencidas ou proximas do vencimento — e a origem
natural do disparo de push pelo aplicativo.

## Fluxo 4 — Tratamento em casa

O veterinario prescreve uma vez. A API **nao guarda "12/12h por 7 dias" como texto**:
transforma a prescricao em 14 registros com data e hora, um para cada administracao.

```
dose  1  30/08 08:00        dose  2  30/08 20:00
dose  3  31/08 08:00        dose  4  31/08 20:00   ...
```

O tutor abre o aplicativo, ve so as doses de hoje e confirma cada uma. Passada a
tolerancia sem confirmacao, a dose vira `PERDIDA` — **sem nenhuma rotina agendada**: a
situacao e deduzida na leitura a partir de `confirmadoEm` e do horario previsto.

No retorno, o veterinario nao le "o tutor disse que deu o remedio", e sim
**adesao de 12 em 14 doses (86%), perdidas as de 30/08 e 31/08** — e sabe se o tratamento
falhou pelo medicamento ou pelo esquecimento.

A adesao considera **apenas as doses ja vencidas**. Cobrar por dose que ainda nem chegou
deixaria todo tratamento com percentual baixo no primeiro dia.

Regras aplicadas (`PlanoDeDoses` e a propria entidade):

- nao confirmar dose futura, nem confirmar a mesma dose duas vezes;
- nao prescrever um medicamento que o pet ja esta tomando;
- intervalo entre 1 e 24 horas, duracao entre 1 e 90 dias, teto de 120 doses por plano;
- prescricao vinculada a consulta exige que ela esteja concluida e seja do mesmo pet;
- tratamento encerrado ou interrompido nao aceita mais confirmacao.

| Metodo | Rota | Perfil |
|---|---|---|
| POST | `/api/v1/tratamentos` | VETERINARIO |
| GET | `/api/v1/tratamentos/hoje` | RESPONSAVEL |
| PATCH | `/api/v1/tratamentos/doses/{doseId}/confirmar` | RESPONSAVEL |
| GET | `/api/v1/tratamentos/{id}` | ambos (dono ou clinica) |
| GET | `/api/v1/tratamentos/pet/{petId}` | ambos (dono ou clinica) |
| PATCH | `/api/v1/tratamentos/{id}/encerrar` | VETERINARIO |
| PATCH | `/api/v1/tratamentos/{id}/interromper` | VETERINARIO |
| GET | `/api/v1/tratamentos/aderencia-baixa` | VETERINARIO |

`/tratamentos/hoje` e a tela principal do fluxo e a origem do lembrete por notificacao.
`/tratamentos/aderencia-baixa` mostra a clinica quem esta deixando doses passar, antes
do retorno acontecer.

---

## Como os fluxos se encaixam

Os quatro nao sao features soltas — cada um alimenta o proximo, e o ultimo volta ao
primeiro:

```
        duvida do tutor
              |
        [1] TRIAGEM  ---- classifica a urgencia
              |              usando idade, vacinacao e tratamento do pet
              v
        [2] AGENDAMENTO ---- nasce com o relato da triagem anexado
              |
              v
          atendimento ---- gera o prontuario na mesma transacao
              |
              v
        [4] TRATAMENTO ---- vira doses no celular do tutor
              |
              +--> a adesao volta para o prontuario
              +--> e passa a influenciar a proxima [1] TRIAGEM

        [3] VACINACAO ---- alimenta [1] e gera as pendencias da clinica
```

O efeito pratico disso e verificavel: **as mesmas respostas de triagem produzem
classificacoes diferentes** conforme o que a API sabe do pet.

---

## Tecnologias

Java 21 · Spring Boot 4 · Spring Security (OAuth2 Resource Server) · Spring Data JPA ·
Flyway · H2 · Swagger/OpenAPI · Maven · Docker

---

## Como executar

O projeto exige **Java 21**. Se o `JAVA_HOME` apontar para outra versao, informe-o
na execucao:

```bash
JAVA_HOME="/caminho/para/jdk-21" ./mvnw spring-boot:run
```

Quando aparecer `Started ChallengeClyvoApplication` no terminal, a API esta no ar.

| Recurso | Endereco |
|---|---|
| Swagger | http://localhost:8080/swagger-ui.html |
| Console H2 | http://localhost:8080/h2-console (JDBC `jdbc:h2:file:./data/challenge_clyvo`, usuario `sa`, sem senha) |

Nao existe pagina em `/`: o projeto e apenas a API.

O banco roda em arquivo na pasta `data/`, entao os dados sobrevivem ao reinicio.
Para comecar do zero, apague essa pasta.

### API publicada

Tambem esta no ar, sem precisar rodar nada:

```
https://java-ollipet.onrender.com/swagger-ui.html
```

A hospedagem gratuita hiberna apos alguns minutos sem uso — a primeira chamada
acorda o servico e pode levar perto de um minuto.

### Variaveis de ambiente

Todas tem valor padrao para o desenvolvimento local; sao usadas ao publicar.

| Variavel | Para que serve |
|---|---|
| `PORT` | Porta do servidor. Plataformas como o Render injetam a sua e derrubam o servico se a aplicacao escutar em outra |
| `DATABASE_URL` | Conexao JDBC. Em disco efemero vale usar `jdbc:h2:mem:ollipet;DB_CLOSE_DELAY=-1` |
| `APP_CORS_ORIGENS` | Origens que podem chamar a API pelo navegador, separadas por virgula |
| `FIREBASE_PROJECT_ID` | Projeto cujos ID tokens a API aceita. Vazio = so os tokens que ela mesma emite |
| `APP_SECURITY_JWT_SECRET` | Chave HMAC dos tokens proprios |

### Deploy com Docker

O `Dockerfile` compila e empacota em duas etapas: a imagem final leva apenas o jar,
sem Maven nem codigo-fonte.

```bash
docker build -t ollipet-api .
docker run -p 8080:8080 ollipet-api
```

### Contas de demonstracao

Criadas pelas migrations `V4` e `V8`. Senha de todas: `123456`.

| Perfil | E-mail |
|---|---|
| Administracao | `admin@ollipet.com` |
| Veterinaria | `camila.duarte@ollipet.com` |
| Veterinario | `rafael.nunes@ollipet.com` |
| Tutora | `maria.silva@email.com` |
| Tutor | `joao.pereira@email.com` |

Para testar no Swagger ou no Insomnia, `POST /api/v1/auth/login` com um desses e-mails
devolve um token; use-o no header `Authorization: Bearer <token>`.

---

## Banco de dados

O schema e criado **exclusivamente** pelo Flyway. O Hibernate roda com
`ddl-auto=validate`, ou seja, apenas confere se as entidades batem com as migrations e
derruba a aplicacao se divergirem.

| Migration | Conteudo |
|---|---|
| `V1__criar_schema_base.sql` | usuario, responsavel, med_vet, pet, prontuario |
| `V2__criar_agendamento_consulta.sql` | consulta e vinculo com o prontuario |
| `V3__criar_carteira_vacinacao.sql` | catalogo de vacinas e doses aplicadas |
| `V4__inserir_dados_iniciais.sql` | usuarios, pets e catalogo de vacinas de demonstracao |
| `V5__criar_triagem.sql` | queixa, perguntas, opcoes, orientacoes, triagem e respostas |
| `V6__inserir_protocolos_triagem.sql` | os 4 protocolos clinicos com 27 perguntas |
| `V7__criar_tratamento.sql` | tratamento e o plano de doses |
| `V8__criar_administrador.sql` | perfil de administracao da clinica |

`Usuario` e uma entidade unica com heranca JOINED: `Responsavel` e `Veterinario` sao
especializacoes dela. Isso permite um unico login para os dois perfis.

---

## Arquitetura

```
controller/   endpoints REST (/api/v1)
service/      regras de negocio
  AvaliadorDeTriagem        pontuacao, sinais de alerta e modificadores
  PoliticaDeAgendamento     restricoes de horario
  AvaliadorSituacaoVacina   criterio de vencimento
  PlanoDeDoses              geracao das doses e apuracao de adesao
  ContaDeUsuarioService     dados comuns a todo usuario
mapper/       conversao entidade <-> DTO
repository/   acesso a dados
model/        entidades e enums (incluem as regras que lhes pertencem)
security/     cadeia stateless, dois emissores de token aceitos
```

Diagrama de classes: `docs/diagrama-classes.png`
Collection do Insomnia: `docs/Insomnia_Collection.yaml`

---

## Equipe

**Olli Pet**

- Andre Rosa Colombo - RM563112
- Gabriely Bonfim Silva - RM566242
- Henrique Rodrigues Vespasiano - RM562917
- Mirelly Sousa Alves - RM566299
- Ruan Luca Feliciano - RM562218
