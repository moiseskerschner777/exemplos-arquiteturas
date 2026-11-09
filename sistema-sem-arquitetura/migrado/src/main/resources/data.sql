INSERT INTO conta (id, titular, saldo, situacao) VALUES ('c1', 'Ana', 100.00, 'ATIVA') ON CONFLICT DO NOTHING;
INSERT INTO conta (id, titular, saldo, situacao) VALUES ('c2', 'Bruno', 50.00, 'ATIVA') ON CONFLICT DO NOTHING;
INSERT INTO conta (id, titular, saldo, situacao) VALUES ('c3', 'Carla', 0.00, 'ENCERRADA') ON CONFLICT DO NOTHING;
