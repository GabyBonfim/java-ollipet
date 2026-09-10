CREATE TABLE tratamento (
    id_tratamento   BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_pet          BIGINT       NOT NULL,
    id_med_vet      BIGINT       NOT NULL,
    id_consulta     BIGINT,
    medicamento     VARCHAR(120) NOT NULL,
    dosagem         VARCHAR(80)  NOT NULL,
    intervalo_horas INT          NOT NULL,
    duracao_dias    INT          NOT NULL,
    inicio_em       TIMESTAMP    NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    observacoes     VARCHAR(500),
    criado_em       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    encerrado_em    TIMESTAMP,
    CONSTRAINT fk_tratamento_pet FOREIGN KEY (id_pet) REFERENCES pet (id_pet),
    CONSTRAINT fk_tratamento_med_vet FOREIGN KEY (id_med_vet) REFERENCES med_vet (id_usuario),
    CONSTRAINT fk_tratamento_consulta FOREIGN KEY (id_consulta) REFERENCES consulta (id_consulta)
);

CREATE INDEX idx_tratamento_pet ON tratamento (id_pet);
CREATE INDEX idx_tratamento_status ON tratamento (status);

-- Uma linha por horario previsto. A prescricao "12/12h por 7 dias" nao fica guardada
-- como texto: vira 14 registros concretos que o tutor confirma um a um.
CREATE TABLE dose_tratamento (
    id_dose          BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_tratamento    BIGINT    NOT NULL,
    numero_dose      INT       NOT NULL,
    horario_previsto TIMESTAMP NOT NULL,
    confirmado_em    TIMESTAMP,
    CONSTRAINT fk_dose_tratamento FOREIGN KEY (id_tratamento) REFERENCES tratamento (id_tratamento),
    CONSTRAINT uk_dose_numero UNIQUE (id_tratamento, numero_dose)
);

CREATE INDEX idx_dose_horario ON dose_tratamento (horario_previsto);
