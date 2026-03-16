# MicroCredit API

API REST para **simulação de análise e concessão de microcrédito**, construída com foco em **arquitetura orientada a eventos, processamento assíncrono e boas práticas de design de software**.

O sistema recebe solicitações de crédito, registra a proposta com status inicial `PENDING` e processa a análise de risco de forma **assíncrona através de mensageria**, garantindo maior resiliência e evitando bloqueio de requisições.

## Objetivos do Projeto

Este projeto foi desenvolvido com foco em demonstrar:

- Arquitetura orientada a eventos (EDA)
- Processamento assíncrono com mensageria
- Aplicação de padrões de projeto
- Boas práticas no desenvolvimento de APIs REST
- Separação clara de responsabilidades entre camadas

---

# Tecnologias Utilizadas

- **Java 21**
- **Spring Boot**
- **RabbitMQ** — Mensageria e processamento assíncrono
- **PostgreSQL** — Banco de dados relacional
- **Flyway** — Versionamento e migração de banco de dados
- **Docker & Docker Compose** — Infraestrutura local
- **Swagger / OpenAPI 3** — Documentação da API
- **JUnit 5 & Mockito** — Testes unitários

---

# Arquitetura

A aplicação foi estruturada visando **escalabilidade, resiliência e manutenção facilitada**.

## Event Driven Architecture (EDA)

Para evitar bloqueio de requisições durante a análise de crédito, o processo foi desacoplado em dois estágios:

1. A API recebe a solicitação de crédito
2. O registro é salvo no banco com status `PENDING`
3. Um evento é publicado no RabbitMQ
4. Um consumer processa a análise de crédito em background
5. O status final é atualizado para `APPROVED` ou `REJECTED`

Fluxo simplificado:

```
Client
  │
  ▼
REST API (Spring Boot)
  │
  │ salva solicitação com status PENDING
  ▼
PostgreSQL
  │
  ▼
RabbitMQ (evento credit-analysis-requested)
  │
  ▼
Credit Analysis Consumer
  │
  ▼
Atualiza status APPROVED / DENIED
```

---

# Padrões de Projeto Aplicados

## Strategy Pattern

Utilizado para encapsular diferentes regras de cálculo de juros e análise de risco.

Interface principal:

```
InterestCalculatorStrategy
```

Cada estratégia implementa uma política diferente de cálculo, permitindo adicionar novas regras de negócio sem modificar as classes existentes.

Isso respeita o **princípio Open/Closed (SOLID)**.

---

## Data Transfer Objects (DTO)

Os DTOs são utilizados para separar a camada de domínio da camada de apresentação.

Benefícios:

- Evita exposição de entidades internas
- Impede vazamento de dados sensíveis
- Previne problemas de serialização em relacionamentos complexos
- Permite evolução independente da API

---

## Global Exception Handling

A aplicação utiliza `@ControllerAdvice` para capturar exceções e padronizar respostas HTTP.

Benefícios:

- Respostas de erro consistentes
- Mensagens claras para consumidores da API
- Centralização do tratamento de exceções

---

# Estrutura do Projeto

```
src/main/java/com/example/microcredit

controller/     -> Endpoints REST
service/        -> Regras de negócio
strategy/       -> Estratégias de cálculo de juros
consumer/       -> Consumidores RabbitMQ
repository/     -> Acesso ao banco de dados
dto/            -> Objetos de transferência de dados
config/         -> Configurações da aplicação
exception/      -> Tratamento global de erros
```

---

# Como Executar o Projeto

## Pré-requisitos

- Java 21
- Maven
- Docker
- Docker Compose

---

## 1. Clonar o repositório

```bash
git clone https://github.com/NavarrosDev/MicroCredit.git
```

```bash
cd microcredit
```

---

## 2. Subir a infraestrutura

```bash
docker-compose up -d
```

Isso iniciará:

- PostgreSQL
- RabbitMQ

---

## 3. Executar a aplicação

```bash
mvn spring-boot:run
```

O **Flyway executará automaticamente as migrações**, criando as tabelas necessárias no banco de dados.

---

## 4. Acessar a documentação da API

Após iniciar a aplicação:

```
http://localhost:8080/swagger-ui/index.html
```

A interface do Swagger permite explorar e testar todos os endpoints.

---

# Testes

Para executar os testes unitários:

```bash
mvn test
```

O projeto utiliza:

- **JUnit 5** para testes unitários
- **Mockito** para criação de mocks e isolamento de dependências

---

# Autor

Desenvolvido por **Gabriel Navarro**
