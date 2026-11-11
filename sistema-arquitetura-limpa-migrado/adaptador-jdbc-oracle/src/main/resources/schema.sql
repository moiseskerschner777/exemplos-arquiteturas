CREATE TABLE conta (
    id        VARCHAR2(36)  PRIMARY KEY,
    titular   VARCHAR2(100) NOT NULL,
    saldo     NUMBER(15,2)  NOT NULL,
    situacao  VARCHAR2(10)  NOT NULL
);

CREATE SEQUENCE seq_movimentacao START WITH 1 INCREMENT BY 1;

CREATE TABLE movimentacao (
    id         NUMBER        PRIMARY KEY,
    conta_id   VARCHAR2(36)  NOT NULL REFERENCES conta (id),
    tipo       VARCHAR2(30)  NOT NULL,
    valor      NUMBER(15,2)  NOT NULL,
    descricao  VARCHAR2(200),
    data_hora  TIMESTAMP     NOT NULL
);

INSERT INTO conta (id, titular, saldo, situacao) VALUES ('c1', 'Ana', 100.00, 'ATIVA');
INSERT INTO conta (id, titular, saldo, situacao) VALUES ('c2', 'Bruno', 50.00, 'ATIVA');
INSERT INTO conta (id, titular, saldo, situacao) VALUES ('c3', 'Carla', 0.00, 'ENCERRADA');
COMMIT;
