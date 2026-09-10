# Protocolos de triagem — conteúdo e contrato

Documento de referência compartilhado entre o app React Native e a API Java.
As perguntas aqui são as mesmas que serão semeadas pelo Flyway, com os mesmos IDs.

> **Enquadramento:** a triagem é uma ferramenta de **orientação**, nunca de diagnóstico.
> Toda resposta da API carrega esse aviso, e classificação `EMERGENCIA` sempre manda ir
> direto à clínica, sem passar por agendamento.

---

## 1. Regra de ouro do contrato

O front **não recebe peso nem sinal de alerta**. Ele recebe pergunta, opções e ordem;
devolve os IDs escolhidos; recebe o resultado pronto.

Isso é proposital: se o app soubesse os pesos, ele calcularia o resultado sozinho e a
API viraria enfeite. A pontuação é do servidor.

---

## 2. Endpoints

| Método | Rota | Quem | Devolve |
|---|---|---|---|
| GET | `/api/v1/triagem/queixas/pet/{petId}` | tutor | cards de queixa filtrados pela espécie do pet |
| GET | `/api/v1/triagem/protocolos/{queixaId}` | tutor | perguntas e opções para montar o formulário |
| POST | `/api/v1/triagem` | tutor | resultado classificado |
| GET | `/api/v1/triagem/{id}` | ambos | uma triagem específica |
| GET | `/api/v1/triagem/minhas` | tutor | histórico das próprias triagens (paginado) |
| GET | `/api/v1/triagem/fila` | veterinário | triagens urgentes ainda não atendidas |

Para transformar a triagem em consulta, o app chama o **agendamento normal** passando
`triagemId` no corpo:

```json
POST /api/v1/consultas
{
  "petId": 1, "veterinarioId": 1,
  "dataHora": "2026-09-15T14:30:00",
  "motivo": "<use o motivoSugerido devolvido pela triagem>",
  "triagemId": 7
}
```

As duas coisas acontecem na mesma transação: se a triagem estiver expirada ou já
encaminhada, a consulta também não é criada.

### GET /api/v1/triagem/protocolos/{queixaId}

```json
{
  "queixaId": 1,
  "nome": "Vômito ou diarreia",
  "descricao": "Enjoo, vômito, fezes moles ou líquidas.",
  "perguntas": [
    {
      "id": 101,
      "texto": "Há quanto tempo começou?",
      "ordem": 1,
      "obrigatoria": true,
      "dependeDeOpcaoId": null,
      "opcoes": [
        { "id": 1011, "texto": "Menos de 12 horas", "ordem": 1 },
        { "id": 1012, "texto": "1 a 2 dias", "ordem": 2 },
        { "id": 1013, "texto": "3 dias ou mais", "ordem": 3 }
      ]
    }
  ]
}
```

`dependeDeOpcaoId` preenchido significa: **só mostre esta pergunta se aquela opção
tiver sido escolhida**. Se o front ignorar o campo e exibir tudo, nada quebra — a API
apenas não cobrará resposta para perguntas cuja condição não foi satisfeita.

### POST /api/v1/triagem

```json
{
  "petId": 1,
  "queixaId": 1,
  "respostas": [
    { "perguntaId": 101, "opcaoId": 1012 },
    { "perguntaId": 102, "opcaoId": 1022 },
    { "perguntaId": 103, "opcaoId": 1031 }
  ]
}
```

Resposta:

```json
{
  "id": 7,
  "petId": 1,
  "nomePet": "Thor",
  "queixa": "Vômito ou diarreia",
  "classificacao": "URGENTE",
  "prazoRecomendado": "24 horas",
  "recomendaConsulta": true,
  "acaoSugerida": "AGENDAR",
  "motivoSugerido": "Vômito há 1 a 2 dias, 3 a 5 episódios, animal quieto — triagem #7",
  "observacoesClinicas": [
    "Vacinação V10 atrasada há 186 dias com sintoma gastrointestinal"
  ],
  "orientacoes": [
    "Retire a comida por 6 horas e mantenha água fresca à vontade",
    "Não ofereça medicamento humano em nenhuma hipótese",
    "Se aparecer sangue ou o animal parar de levantar, procure atendimento imediato"
  ],
  "aviso": "Esta orientação não substitui avaliação veterinária presencial.",
  "expiraEm": "2026-08-29T14:30:00",
  "status": "CLASSIFICADA"
}
```

`pontuacao` **não vai na resposta do tutor** — número solto assusta e não ajuda. Ele
aparece só na fila do veterinário.

---

## 3. Classificações

| Classificação | Prazo | `acaoSugerida` | O app mostra |
|---|---|---|---|
| `EMERGENCIA` | imediato | `IR_AGORA` | telefone e endereço da clínica, sem botão de agendar |
| `URGENTE` | 24 horas | `AGENDAR` | botão de agendar em destaque |
| `POUCO_URGENTE` | 3 a 5 dias | `AGENDAR` | botão de agendar normal |
| `ORIENTACAO` | — | `ACOMPANHAR` | só as orientações e "refaça se piorar" |

Qualquer opção marcada como **sinal de alerta** força `EMERGENCIA`, independente da
pontuação somada.

---

## 4. Queixa 1 — Vômito ou diarreia (id 1)

*Espécies: cão e gato · limite urgente: 12 · limite pouco urgente: 6*

### 101. Há quanto tempo começou?
| id | Opção | Peso |
|---|---|---|
| 1011 | Menos de 12 horas | 1 |
| 1012 | 1 a 2 dias | 2 |
| 1013 | 3 dias ou mais | 4 |

### 102. Quantos episódios nas últimas 24 horas?
| id | Opção | Peso |
|---|---|---|
| 1021 | 1 ou 2 | 1 |
| 1022 | 3 a 5 | 3 |
| 1023 | Mais de 5 | 5 |

### 103. Tem sangue no vômito ou nas fezes?
| id | Opção | Peso |
|---|---|---|
| 1031 | Não | 0 |
| 1032 | Sim, pouco ou raiado | 3 |
| 1033 | Sim, bastante — ou fezes pretas | 🚨 **alerta** |

### 104. Está conseguindo beber água?
| id | Opção | Peso |
|---|---|---|
| 1041 | Sim, normalmente | 0 |
| 1042 | Bebe pouco | 2 |
| 1043 | Não bebe nada há mais de 12 horas | 4 |

### 105. Está tentando vomitar sem sair nada, com a barriga inchada e dura?
| id | Opção | Peso |
|---|---|---|
| 1051 | Não | 0 |
| 1052 | Sim | 🚨 **alerta** |

> Essa é a pergunta mais importante do protocolo. Descreve torção gástrica, que mata
> em horas. Vale a pena o app mostrar um ícone de atenção nela.

### 106. Como está o ânimo?
| id | Opção | Peso |
|---|---|---|
| 1061 | Normal, brincando | 0 |
| 1062 | Mais quieto, mas levanta e anda | 2 |
| 1063 | Não levanta, muito prostrado | 🚨 **alerta** |

### 107. Comeu algo diferente nos últimos 3 dias?
| id | Opção | Peso |
|---|---|---|
| 1071 | Não, ou não sei | 0 |
| 1072 | Ração nova ou petisco diferente | 1 |
| 1073 | Lixo, planta, osso ou brinquedo | 3 |
| 1074 | Veneno, remédio humano, chocolate ou uva | 🚨 **alerta** |

**Orientações (ORIENTACAO / POUCO_URGENTE):**
- Retire a comida por 6 horas e mantenha água fresca à vontade
- Ao voltar a alimentar, ofereça porções pequenas de comida leve
- Não ofereça medicamento humano em nenhuma hipótese
- Se aparecer sangue ou o animal parar de levantar, procure atendimento imediato

---

## 5. Queixa 2 — Pele e coceira (id 2)

*Espécies: cão e gato · limite urgente: 12 · limite pouco urgente: 5*

### 201. Há quanto tempo?
| id | Opção | Peso |
|---|---|---|
| 2011 | Menos de 1 semana | 1 |
| 2012 | 1 a 4 semanas | 2 |
| 2013 | Mais de 1 mês | 3 |

### 202. Quanto ele se coça?
| id | Opção | Peso |
|---|---|---|
| 2021 | De vez em quando | 1 |
| 2022 | Bastante, mas dorme normal | 2 |
| 2023 | Sem parar, não consegue dormir | 4 |

### 203. Tem ferida, casquinha ou pus?
| id | Opção | Peso |
|---|---|---|
| 2031 | Não, só vermelhidão | 1 |
| 2032 | Ferida seca ou casquinha | 2 |
| 2033 | Ferida com pus ou cheiro ruim | 4 |

### 204. Está perdendo pelo?
| id | Opção | Peso |
|---|---|---|
| 2041 | Não | 0 |
| 2042 | Sim, em um lugar só | 2 |
| 2043 | Sim, em vários lugares | 3 |

### 205. Onde está concentrado?
| id | Opção | Peso |
|---|---|---|
| 2051 | Orelhas | 2 |
| 2052 | Patas | 2 |
| 2053 | Barriga ou virilha | 2 |
| 2054 | Base do rabo | 2 |
| 2055 | Espalhado pelo corpo | 3 |

### 206. Além da coceira, está diferente?
| id | Opção | Peso |
|---|---|---|
| 2061 | Não, come e brinca normal | 0 |
| 2062 | Mais quieto ou comendo menos | 3 |

### 207. Apareceu inchaço no focinho ou dificuldade para respirar?
| id | Opção | Peso |
|---|---|---|
| 2071 | Não | 0 |
| 2072 | Sim, inchaço no rosto ou focinho | 🚨 **alerta** |
| 2073 | Sim, está respirando com dificuldade | 🚨 **alerta** |

> Reação alérgica aguda. Raro, mas quando acontece é questão de minutos.

**Orientações (ORIENTACAO / POUCO_URGENTE):**
- Evite banhos com shampoo humano ou produtos perfumados
- Use colar elizabetano se ele estiver se machucando ao coçar
- Verifique se a proteção contra pulgas está em dia
- Fotografe as lesões hoje, ajuda a vet a comparar na consulta

---

## 6. Queixa 3 — Não está comendo (id 3)

*Espécies: cão e gato · limite urgente: 11 · limite pouco urgente: 5*

### 301. Há quanto tempo está sem comer direito?
| id | Opção | Peso |
|---|---|---|
| 3011 | Menos de 24 horas | 2 |
| 3012 | 1 a 2 dias | 4 |
| 3013 | 3 dias ou mais | 5 |

> Em **gatos**, 3 dias ou mais recebe peso extra na API — jejum prolongado em felino
> leva a lipidose hepática. É uma das regras que o front não precisa conhecer.

### 302. Está bebendo água?
| id | Opção | Peso |
|---|---|---|
| 3021 | Sim, normalmente | 0 |
| 3022 | Bebe pouco | 2 |
| 3023 | Não bebe nada | 4 |

### 303. Recusa tudo ou só a ração?
| id | Opção | Peso |
|---|---|---|
| 3031 | Come petisco ou comida caseira, recusa só a ração | 1 |
| 3032 | Recusa tudo que é oferecido | 4 |

### 304. Notou perda de peso?
| id | Opção | Peso |
|---|---|---|
| 3041 | Não | 0 |
| 3042 | Um pouco | 2 |
| 3043 | Sim, bastante | 4 |

### 305. Tem outro sintoma junto?
| id | Opção | Peso |
|---|---|---|
| 3051 | Não, só não come | 0 |
| 3052 | Vômito ou diarreia | 3 |
| 3053 | Baba, mexe muito a boca ou parece doer para mastigar | 3 |
| 3054 | Está prostrado, não levanta | 🚨 **alerta** |

### 306. Mudou algo na rotina recentemente?
| id | Opção | Peso |
|---|---|---|
| 3061 | Não | 0 |
| 3062 | Sim — mudança de casa, viagem, pet novo ou obra | 1 |

**Orientações (ORIENTACAO / POUCO_URGENTE):**
- Ofereça a comida morna, o cheiro estimula o apetite
- Deixe água fresca em mais de um ponto da casa
- Não force a alimentação com seringa sem orientação da vet
- Anote quanto ele comeu por dia até a consulta

---

## 7. Queixa 4 — Respiração e tosse (id 4)

*Espécies: cão e gato · limite urgente: 9 · limite pouco urgente: 4*

> Protocolo mais sensível dos quatro: o limite de urgência é baixo de propósito.

### 401. Como está a respiração agora?
| id | Opção | Peso |
|---|---|---|
| 4011 | Normal | 0 |
| 4012 | Mais rápida que o normal | 4 |
| 4013 | Com esforço, barulho ou de boca aberta | 🚨 **alerta** |

> Gato respirando de boca aberta é emergência sempre, sem exceção.

### 402. A língua ou a gengiva está arroxeada ou azulada?
| id | Opção | Peso |
|---|---|---|
| 4021 | Não, está rosada | 0 |
| 4022 | Sim, arroxeada, azulada ou muito pálida | 🚨 **alerta** |
| 4023 | Não consegui ver | 0 |

### 403. Tem tosse?
| id | Opção | Peso |
|---|---|---|
| 4031 | Não | 0 |
| 4032 | Tosse seca, de vez em quando | 2 |
| 4033 | Tosse frequente | 3 |
| 4034 | Tosse com secreção ou espuma | 🚨 **alerta** |

### 404. Há quanto tempo?
| id | Opção | Peso |
|---|---|---|
| 4041 | Começou hoje | 3 |
| 4042 | Alguns dias | 2 |
| 4043 | Semanas | 2 |

### 405. Cansa mais fácil que antes no passeio ou brincando?
| id | Opção | Peso |
|---|---|---|
| 4051 | Não | 0 |
| 4052 | Sim, cansa mais rápido | 3 |

### 406. Está espirrando ou com secreção no nariz e olhos?
| id | Opção | Peso |
|---|---|---|
| 4061 | Não | 0 |
| 4062 | Sim, transparente | 1 |
| 4063 | Sim, amarelada ou esverdeada | 3 |

### 407. Pode ter engasgado com algum objeto ou alimento?
| id | Opção | Peso |
|---|---|---|
| 4071 | Não | 0 |
| 4072 | Sim, ou não tenho certeza | 🚨 **alerta** |

**Orientações (ORIENTACAO / POUCO_URGENTE):**
- Deixe o pet em local arejado e fresco, evite esforço e passeios
- Troque coleira por peitoral enquanto houver tosse
- Evite fumaça, perfume e produtos de limpeza fortes no ambiente
- Grave um vídeo curto da respiração ou da tosse para mostrar à vet

---

## 8. Modificadores aplicados pela API

Isso é o que o app **não conseguiria calcular sozinho** — depende de dados que só a API
tem. Somado à pontuação das respostas e listado em `observacoesClinicas`.

| Condição | Ajuste | Justificativa exibida |
|---|---|---|
| Pet com menos de 6 meses | +3 | "Filhote com N meses: descompensa mais rápido que um adulto" |
| Pet com 8 anos ou mais | +2 | "Animal idoso (N anos): avaliar com mais cautela" |
| Queixa gastrointestinal + vacinação atrasada | +5 | "Sintoma gastrointestinal com vacinação em atraso (V10 atrasada há N dias)" |
| Gato + sem comer há 3 dias ou mais | +4 | "Jejum prolongado em felino: risco de lipidose hepática" |
| Consulta concluída nos últimos 15 dias | — | `acaoSugerida = AGENDAR_RETORNO` |

> O modificador de tratamento em curso entra junto com o fluxo de tratamento, que ainda
> não foi implementado.

---

## 9. Validações da API

| Situação | Resposta |
|---|---|
| Pet é de outro tutor | 403 |
| Queixa não se aplica à espécie do pet | 409 |
| Faltou responder pergunta obrigatória exigível | 400, listando quais |
| Opção não pertence à pergunta informada | 400 |
| Duas respostas para a mesma pergunta | 400 |
| Encaminhar triagem já encaminhada | 409 |
| Encaminhar triagem expirada (mais de 24h) | 409 |
| Mais de 10 triagens do mesmo pet em 24h | 429 |

---

## 10. Telas sugeridas no app

1. **Escolha da queixa** — grade de cards vinda de `GET /triagem/queixas`
2. **Formulário** — uma pergunta por vez ou lista rolável, botões de opção única
3. **Resultado** — cor da faixa conforme a classificação, orientações em lista,
   botão de ação conforme `acaoSugerida`
4. **Histórico** — `GET /triagem/minhas`, com o que virou consulta marcado

Cores sugeridas: `EMERGENCIA` vermelho · `URGENTE` laranja · `POUCO_URGENTE` amarelo ·
`ORIENTACAO` verde.
