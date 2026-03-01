-- Inserindo o João (Risco Baixo, vai ser aprovado) e a Maria (Restrição no Serasa, vai ser negada

INSERT INTO tb_client (id, name, cpf, monthly_income)
VALUES
('a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d', 'João Silva (Risco Baixo)', '12345678900', 6000.00),
('f5e4d3c2-b1a0-4c3d-9e8f-7a6b5c4d3e2f', 'Maria Souza (Serasa)', '98765432199', 3000.00);