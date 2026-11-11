create table pacientes (
    id bigint not null auto_increment,
    nome varchar(100) not null,
    email varchar(100) not null unique,
    cpf varchar(11) not null unique,
    logradouro varchar(100) not null,
    bairro varchar(100) not null,
    cep varchar(8) not null,
    cidade varchar(100) not null,
    uf char(2) not null,
    numero varchar(20),
    complemento varchar(100),
    telefone varchar(20) not null,
    ativo tinyint not null default 1,
    primary key (id)
);
