-- Senha de todos os usuarios de demonstracao: 123456 (BCrypt)
INSERT INTO usuario (tipo, nome, email, cpf, senha) VALUES
    ('VETERINARIO', 'Dra. Camila Duarte', 'camila.duarte@ollipet.com', '98765432100', '$2a$10$zztCBVlHLsW6w.9BUUtAsurw7KlpNhZ19Ld9yYpZtzDVptGkGXRpe'),
    ('VETERINARIO', 'Dr. Rafael Nunes',   'rafael.nunes@ollipet.com',  '45678912311', '$2a$10$zztCBVlHLsW6w.9BUUtAsurw7KlpNhZ19Ld9yYpZtzDVptGkGXRpe'),
    ('RESPONSAVEL', 'Maria Silva',        'maria.silva@email.com',     '12345678901', '$2a$10$zztCBVlHLsW6w.9BUUtAsurw7KlpNhZ19Ld9yYpZtzDVptGkGXRpe'),
    ('RESPONSAVEL', 'Joao Pereira',       'joao.pereira@email.com',    '32165498700', '$2a$10$zztCBVlHLsW6w.9BUUtAsurw7KlpNhZ19Ld9yYpZtzDVptGkGXRpe');

INSERT INTO med_vet (id_usuario, crmv, especialidade)
SELECT id_usuario, 'SP-12345', 'Clinica Geral' FROM usuario WHERE email = 'camila.duarte@ollipet.com';
INSERT INTO med_vet (id_usuario, crmv, especialidade)
SELECT id_usuario, 'SP-54321', 'Dermatologia' FROM usuario WHERE email = 'rafael.nunes@ollipet.com';

INSERT INTO responsavel (id_usuario, data_nasc)
SELECT id_usuario, DATE '1995-08-10' FROM usuario WHERE email = 'maria.silva@email.com';
INSERT INTO responsavel (id_usuario, data_nasc)
SELECT id_usuario, DATE '1988-02-27' FROM usuario WHERE email = 'joao.pereira@email.com';

INSERT INTO pet (nome, descricao, raca, especie, data_nasc, id_resp)
SELECT 'Thor', 'Cachorro docil, castrado', 'Golden Retriever', 'CAO', DATE '2021-03-15', id_usuario
FROM usuario WHERE email = 'maria.silva@email.com';
INSERT INTO pet (nome, descricao, raca, especie, data_nasc, id_resp)
SELECT 'Nina', 'Gata arisca com estranhos', 'Siames', 'GATO', DATE '2022-07-01', id_usuario
FROM usuario WHERE email = 'maria.silva@email.com';
INSERT INTO pet (nome, descricao, raca, especie, data_nasc, id_resp)
SELECT 'Bud', 'Idoso, acompanhamento cardiaco', 'Poodle', 'CAO', DATE '2019-11-20', id_usuario
FROM usuario WHERE email = 'joao.pereira@email.com';

-- Protocolo: doses_protocolo = quantidade da serie inicial, intervalo_dias = espera entre elas,
-- meses_reforco = periodicidade do reforco depois da serie concluida.
INSERT INTO vacina (nome, especie, doses_protocolo, intervalo_dias, meses_reforco) VALUES
    ('V10',              'CAO',  3, 21, 12),
    ('Antirrabica',      'CAO',  1,  0, 12),
    ('Giardia',          'CAO',  2, 21, 12),
    ('Gripe Canina',     'CAO',  2, 21, 12),
    ('V4',               'GATO', 3, 21, 12),
    ('Antirrabica',      'GATO', 1,  0, 12),
    ('Leucemia Felina',  'GATO', 2, 21, 12);
