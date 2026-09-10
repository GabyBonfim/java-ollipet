-- Perfil de administracao da clinica.
--
-- Cadastrar veterinario deixou de ser uma acao publica: qualquer pessoa que
-- baixasse o aplicativo poderia se declarar medico. A operacao passa a exigir
-- este perfil, que so a clinica possui.
--
-- Na heranca JOINED o Hibernate exige uma tabela por subclasse, mesmo quando ela
-- nao acrescenta nenhum atributo: a tabela guarda apenas a chave estrangeira.

CREATE TABLE administrador (
    id_usuario BIGINT PRIMARY KEY,
    CONSTRAINT fk_administrador_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);

-- Senha: 123456 (mesmo hash das demais contas de demonstracao)
INSERT INTO usuario (tipo, nome, email, cpf, senha) VALUES
    ('ADMIN', 'Administracao Olli Pet', 'admin@ollipet.com', '00011122233',
     '$2a$10$zztCBVlHLsW6w.9BUUtAsurw7KlpNhZ19Ld9yYpZtzDVptGkGXRpe');

INSERT INTO administrador (id_usuario)
SELECT id_usuario FROM usuario WHERE email = 'admin@ollipet.com';
