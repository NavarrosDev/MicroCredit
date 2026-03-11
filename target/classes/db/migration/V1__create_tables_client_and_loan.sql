CREATE TABLE tb_client (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    monthly_income DECIMAL(15, 2) NOT NULL
);

CREATE TABLE tb_loan (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL,
    requested_amount DECIMAL(15, 2) NOT NULL,
    total_value_including_interest DECIMAL(15, 2) NOT NULL,
    number_of_installments INTEGER NOT NULL,
    state_loan VARCHAR(30) NOT NULL,
    CONSTRAINT fk_loan_client FOREIGN KEY (client_id) REFERENCES tb_client (id)
);