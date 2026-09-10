CREATE TABLE queixa (
    id_queixa            BIGINT       PRIMARY KEY,
    nome                 VARCHAR(80)  NOT NULL,
    descricao            VARCHAR(255) NOT NULL,
    categoria            VARCHAR(30)  NOT NULL,
    especie              VARCHAR(20),
    limite_urgente       INT          NOT NULL,
    limite_pouco_urgente INT          NOT NULL,
    ordem                INT          NOT NULL,
    CONSTRAINT uk_queixa_nome UNIQUE (nome)
);

-- As perguntas e opcoes tem id fixo, definido no protocolo clinico e nao gerado pelo
-- banco: o aplicativo monta as telas em cima desses numeros.
CREATE TABLE pergunta_triagem (
    id_pergunta          BIGINT       PRIMARY KEY,
    id_queixa            BIGINT       NOT NULL,
    texto                VARCHAR(255) NOT NULL,
    ordem                INT          NOT NULL,
    obrigatoria          BOOLEAN      NOT NULL DEFAULT TRUE,
    id_opcao_dependencia BIGINT,
    CONSTRAINT fk_pergunta_queixa FOREIGN KEY (id_queixa) REFERENCES queixa (id_queixa)
);

CREATE TABLE opcao_resposta (
    id_opcao     BIGINT       PRIMARY KEY,
    id_pergunta  BIGINT       NOT NULL,
    texto        VARCHAR(120) NOT NULL,
    peso         INT          NOT NULL DEFAULT 0,
    sinal_alerta BOOLEAN      NOT NULL DEFAULT FALSE,
    marcador     VARCHAR(40),
    ordem        INT          NOT NULL,
    CONSTRAINT fk_opcao_pergunta FOREIGN KEY (id_pergunta) REFERENCES pergunta_triagem (id_pergunta)
);

-- A dependencia entre pergunta e opcao so pode ser criada depois das duas tabelas.
ALTER TABLE pergunta_triagem
    ADD CONSTRAINT fk_pergunta_dependencia
    FOREIGN KEY (id_opcao_dependencia) REFERENCES opcao_resposta (id_opcao);

CREATE TABLE orientacao_triagem (
    id_orientacao BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_queixa     BIGINT       NOT NULL,
    classificacao VARCHAR(20)  NOT NULL,
    texto         VARCHAR(255) NOT NULL,
    ordem         INT          NOT NULL,
    CONSTRAINT fk_orientacao_queixa FOREIGN KEY (id_queixa) REFERENCES queixa (id_queixa)
);

CREATE INDEX idx_orientacao_queixa ON orientacao_triagem (id_queixa, classificacao);

CREATE TABLE triagem (
    id_triagem    BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_pet        BIGINT      NOT NULL,
    id_queixa     BIGINT      NOT NULL,
    pontuacao     INT         NOT NULL,
    classificacao VARCHAR(20) NOT NULL,
    acao_sugerida VARCHAR(20) NOT NULL,
    status        VARCHAR(20) NOT NULL,
    criado_em     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_em     TIMESTAMP   NOT NULL,
    id_consulta   BIGINT,
    CONSTRAINT fk_triagem_pet FOREIGN KEY (id_pet) REFERENCES pet (id_pet),
    CONSTRAINT fk_triagem_queixa FOREIGN KEY (id_queixa) REFERENCES queixa (id_queixa),
    CONSTRAINT fk_triagem_consulta FOREIGN KEY (id_consulta) REFERENCES consulta (id_consulta),
    CONSTRAINT uk_triagem_consulta UNIQUE (id_consulta)
);

CREATE INDEX idx_triagem_pet ON triagem (id_pet);
CREATE INDEX idx_triagem_fila ON triagem (classificacao, status);

CREATE TABLE resposta_triagem (
    id_resposta BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_triagem  BIGINT NOT NULL,
    id_pergunta BIGINT NOT NULL,
    id_opcao    BIGINT NOT NULL,
    CONSTRAINT fk_resposta_triagem FOREIGN KEY (id_triagem) REFERENCES triagem (id_triagem),
    CONSTRAINT fk_resposta_pergunta FOREIGN KEY (id_pergunta) REFERENCES pergunta_triagem (id_pergunta),
    CONSTRAINT fk_resposta_opcao FOREIGN KEY (id_opcao) REFERENCES opcao_resposta (id_opcao),
    CONSTRAINT uk_resposta_pergunta UNIQUE (id_triagem, id_pergunta)
);

-- Observacoes que a API deduz cruzando a triagem com o cadastro do pet
-- (idade, vacinacao em atraso). Ficam gravadas porque dependem do estado do
-- pet no momento da triagem e nao poderiam ser recalculadas depois.
CREATE TABLE observacao_triagem (
    id_observacao BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_triagem    BIGINT       NOT NULL,
    texto         VARCHAR(255) NOT NULL,
    CONSTRAINT fk_observacao_triagem FOREIGN KEY (id_triagem) REFERENCES triagem (id_triagem)
);
