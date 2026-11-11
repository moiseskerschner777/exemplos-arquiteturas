CREATE TABLE conta (
    id        VARCHAR(36)   PRIMARY KEY,
    titular   VARCHAR(100)  NOT NULL,
    saldo     NUMERIC(15,2) NOT NULL,
    situacao  VARCHAR(10)   NOT NULL
);

CREATE SEQUENCE seq_movimentacao START WITH 1 INCREMENT BY 1;

CREATE TABLE movimentacao (
    id         BIGINT        PRIMARY KEY,
    conta_id   VARCHAR(36)   NOT NULL REFERENCES conta (id),
    tipo       VARCHAR(30)   NOT NULL,
    valor      NUMERIC(15,2) NOT NULL,
    descricao  VARCHAR(200),
    data_hora  TIMESTAMP     NOT NULL
);