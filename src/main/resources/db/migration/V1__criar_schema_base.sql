CREATE TABLE usuario (
    id_usuario   BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo         VARCHAR(20)  NOT NULL,
    nome         VARCHAR(100) NOT NULL,
    email        VARCHAR(120) NOT NULL,
    cpf          VARCHAR(11)  NOT NULL,
    senha        VARCHAR(100),
    firebase_uid VARCHAR(128),
    ativo        BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT uk_usuario_cpf UNIQUE (cpf),
    CONSTRAINT uk_usuario_firebase_uid UNIQUE (firebase_uid)
);

CREATE TABLE responsavel (
    id_usuario BIGINT PRIMARY KEY,
    data_nasc  DATE,
    CONSTRAINT fk_responsavel_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);

CREATE TABLE med_vet (
    id_usuario    BIGINT      PRIMARY KEY,
    crmv          VARCHAR(20) NOT NULL,
    especialidade VARCHAR(80),
    CONSTRAINT fk_med_vet_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT uk_med_vet_crmv UNIQUE (crmv)
);

CREATE TABLE pet (
    id_pet    BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome      VARCHAR(80) NOT NULL,
    descricao VARCHAR(255),
    raca      VARCHAR(80) NOT NULL,
    especie   VARCHAR(20) NOT NULL,
    data_nasc DATE        NOT NULL,
    id_resp   BIGINT      NOT NULL,
    CONSTRAINT fk_pet_responsavel FOREIGN KEY (id_resp) REFERENCES responsavel (id_usuario)
);

CREATE INDEX idx_pet_responsavel ON pet (id_resp);

CREATE TABLE prontuario (
    id_prontuario     BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_pet            BIGINT       NOT NULL,
    id_med_vet        BIGINT       NOT NULL,
    procedimento      VARCHAR(255) NOT NULL,
    data_procedimento DATE         NOT NULL,
    local_atendimento VARCHAR(120) NOT NULL,
    CONSTRAINT fk_prontuario_pet FOREIGN KEY (id_pet) REFERENCES pet (id_pet),
    CONSTRAINT fk_prontuario_med_vet FOREIGN KEY (id_med_vet) REFERENCES med_vet (id_usuario)
);

CREATE INDEX idx_prontuario_pet ON prontuario (id_pet);
