# E-commerce CQRS Event Sourcing

Este projeto é um sistema de e-commerce baseado em Event Sourcing e CQRS, utilizando Java 17 e Spring Boot.

## Pré-requisitos

- Java 17
- Maven 3.6+
- Docker

## Como rodar localmente

1. Clone o repositório:
   ```bash
   git clone <URL_DO_REPOSITORIO>
   ```

2. Navegue até o diretório do projeto:
   ```bash
   cd ecommerce-cqrs-eventsourcing
   ```

3. Inicie os serviços de infraestrutura com Docker Compose:
   ```bash
   docker-compose up -d
   ```

4. Compile e rode os módulos do projeto:
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run -pl order-command-service
   ```

## Estrutura do Projeto

- **shared**: Módulo com classes e interfaces compartilhadas.
- **order-command-service**: Serviço de comando para gerenciamento de pedidos.
- **order-projection**: Serviço de projeção para consultas rápidas.
- **saga-orchestrator**: Orquestrador de sagas para transações distribuídas.
- **infra**: Configurações de infraestrutura e scripts.

## Contribuição

Contribuições são bem-vindas! Por favor, siga as diretrizes de contribuição e mantenha o código limpo e documentado.
