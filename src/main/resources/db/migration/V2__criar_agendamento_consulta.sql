CREATE TABLE consulta (
    id_consulta   BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_pet        BIGINT       NOT NULL,
    id_med_vet    BIGINT       NOT NULL,
    data_hora     TIMESTAMP    NOT NULL,
    motivo        VARCHAR(255) NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    observacoes   VARCHAR(500),
    criado_em     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP,
    CONSTRAINT fk_consulta_pet FOREIGN KEY (id_pet) REFERENCES pet (id_pet),
    CONSTRAINT fk_consulta_med_vet FOREIGN KEY (id_med_vet) REFERENCES med_vet (id_usuario)
);

CREATE INDEX idx_consulta_agenda ON consulta (id_med_vet, data_hora);
CREATE INDEX idx_consulta_pet ON consulta (id_pet);
CREATE INDEX idx_consulta_status ON consulta (status);

ALTER TABLE prontuario ADD COLUMN id_consulta BIGINT;

ALTER TABLE prontuario
    ADD CONSTRAINT fk_prontuario_consulta FOREIGN KEY (id_consulta) REFERENCES consulta (id_consulta);

ALTER TABLE prontuario ADD CONSTRAINT uk_prontuario_consulta UNIQUE (id_consulta);
