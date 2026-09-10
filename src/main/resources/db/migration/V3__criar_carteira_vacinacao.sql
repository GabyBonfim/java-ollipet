CREATE TABLE vacina (
    id_vacina       BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome            VARCHAR(80) NOT NULL,
    especie         VARCHAR(20) NOT NULL,
    doses_protocolo INT         NOT NULL,
    intervalo_dias  INT         NOT NULL,
    meses_reforco   INT,
    CONSTRAINT uk_vacina_nome_especie UNIQUE (nome, especie)
);

CREATE TABLE aplicacao_vacina (
    id_aplicacao   BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_pet         BIGINT      NOT NULL,
    id_vacina      BIGINT      NOT NULL,
    id_med_vet     BIGINT      NOT NULL,
    numero_dose    INT         NOT NULL,
    data_aplicacao DATE        NOT NULL,
    proxima_dose   DATE,
    lote           VARCHAR(40),
    CONSTRAINT fk_aplicacao_pet FOREIGN KEY (id_pet) REFERENCES pet (id_pet),
    CONSTRAINT fk_aplicacao_vacina FOREIGN KEY (id_vacina) REFERENCES vacina (id_vacina),
    CONSTRAINT fk_aplicacao_med_vet FOREIGN KEY (id_med_vet) REFERENCES med_vet (id_usuario),
    CONSTRAINT uk_aplicacao_dose UNIQUE (id_pet, id_vacina, numero_dose)
);

CREATE INDEX idx_aplicacao_pet ON aplicacao_vacina (id_pet);
CREATE INDEX idx_aplicacao_proxima_dose ON aplicacao_vacina (proxima_dose);
