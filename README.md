# RentaYa API

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-4169E1?logo=postgresql&logoColor=white)

API REST para a gestão da frota de uma concessionária de veículos em São Paulo:
cadastro de carros, motos e caminhões, tarifas, quilometragem, custo de
manutenção e estatísticas por marca.

## Stack

- **Java 21** e **Spring Boot 4** (Spring Web MVC, Spring Data JPA, Bean Validation, Actuator)
- **PostgreSQL** gerenciado no **Neon**, com `ddl-auto=validate`
- **JUnit 5 + MockMvc** com banco **H2** em memória nos testes
- Coleção do **Postman** com testes automatizados

## Arquitetura

```
controller  →  service  →  repository  →  PostgreSQL
    ↑              ↑
   DTOs         mapper / regras de negócio no modelo
```

- **controller:** expõe os endpoints e valida a entrada (`@Valid`).
- **service:** casos de uso e transações (`@Transactional`).
- **repository:** acesso a dados com Spring Data JPA, incluindo consulta JPQL
  por tipo (`TYPE(v)`) e consulta nativa para estatísticas.
- **model:** hierarquia `Vehiculo` → `Auto`, `Moto` e `Camion`, com herança
  `SINGLE_TABLE`. Cada tipo calcula o próprio custo de manutenção (polimorfismo).
- **exception:** tratamento global de erros com respostas padronizadas.

## Endpoints

Base: `/api/v1/vehiculos`

| Método | Rota | Descrição | Respostas |
|---|---|---|---|
| POST | `/` | Cadastra um veículo (`tipo`: AUTO, MOTO ou CAMION) | 201 + `Location`, 400, 409 |
| GET | `/{id}` | Busca um veículo ativo | 200, 404 |
| GET | `/?tipo=AUTO` | Lista os veículos ativos, com filtro opcional por tipo | 200, 400 |
| PUT | `/{id}/tarifa` | Altera a tarifa diária | 200, 400, 404 |
| POST | `/{id}/kilometros` | Soma quilômetros ao hodômetro | 200, 400, 404 |
| GET | `/{id}/mantenimiento` | Calcula o custo de manutenção | 200, 404 |
| DELETE | `/{id}` | Exclusão lógica (o veículo deixa de aparecer) | 204, 404 |
| GET | `/estadisticas/por-marca` | Média de quilometragem e cilindrada por marca | 200 |

**Custo de manutenção por tipo**

| Tipo | Fórmula |
|---|---|
| Auto | 100 + km × 0,05 |
| Moto | 50 + km × 0,03 |
| Camión | 300 + km × 0,10 + toneladas × 20 |

### Exemplo

```http
POST /api/v1/vehiculos
Content-Type: application/json

{
  "tipo": "AUTO",
  "placa": "ABC-123",
  "marca": "Toyota",
  "modelo": "Corolla",
  "anio": 2022,
  "tarifaDiariaBase": 120.00,
  "numeroPuertas": 4
}
```

```json
{
  "id": 1,
  "tipo": "AUTO",
  "placa": "ABC-123",
  "marca": "Toyota",
  "modelo": "Corolla",
  "anio": 2022,
  "tarifaDiariaBase": 120.00,
  "kilometraje": 0.00,
  "descripcion": "Toyota Corolla (2022) - ABC-123",
  "numeroPuertas": 4
}
```

### Erros

Todos os erros seguem o mesmo formato:

```json
{
  "timestamp": "2026-10-26T17:43:12",
  "status": 400,
  "error": "Bad Request",
  "message": "Datos inválidos",
  "path": "/api/v1/vehiculos",
  "errores": { "placa": "debe tener el formato AAA-999" }
}
```

| Situação | Status |
|---|---|
| Campos inválidos ou JSON malformado | 400 |
| Tipo de veículo inexistente ou regra de negócio violada | 400 |
| Veículo não encontrado ou excluído | 404 |
| Placa já cadastrada | 409 |

## Como executar

1. Crie um arquivo `.env` na raiz a partir do `.env.example`:
   ```
   DB_URL=jdbc:postgresql://<host>/<database>?sslmode=require
   DB_USER=<usuario>
   DB_PASSWORD=<senha>
   ```
2. Rode a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```
3. A API fica em `http://localhost:8080` e a saúde em `/actuator/health`.

## Testes

```bash
./mvnw test
```

`VehiculoControllerTest` cobre os 12 cenários da API (criação de cada tipo,
validações, placa duplicada, busca, filtro, tarifa, quilometragem, manutenção e
exclusão lógica) contra um **H2 em memória**: os testes nunca tocam o banco real.

A pasta `postman/` contém a coleção e o ambiente. Para rodar todos os cenários
com o Postman CLI:

```bash
postman collection run postman/rentaya-api.postman_collection.json \
  -e postman/rentaya-local.postman_environment.json
```

## Decisões de projeto

**Por que DTOs em vez de expor as entidades?**
Os DTOs separam o contrato da API do modelo de banco. A entrada aceita apenas
os campos que o cliente pode enviar (ele não define `id`, `kilometraje` nem
`activo`), as validações ficam no ponto de entrada e cada tipo de veículo tem o
seu formato (`numeroPuertas`, `cilindrada`, `capacidadToneladas`). Na saída, a
API devolve campos calculados como `descripcion` sem expor detalhes internos do
JPA. Assim é possível mudar o banco sem quebrar quem consome a API.

**Herança `SINGLE_TABLE`: vantagem e desvantagem**
- **Vantagem:** todos os veículos ficam em uma única tabela `vehiculos`,
  diferenciados pela coluna `tipo`. As consultas e listagens são simples e
  rápidas, sem `JOIN`, e consultas polimórficas (todos os veículos de uma marca,
  por exemplo) são naturais.
- **Desvantagem:** as colunas específicas de cada subtipo (`numero_puertas`,
  `cilindrada`, `capacidad_toneladas`) precisam aceitar `NULL`, porque não se
  aplicam aos outros tipos. Com isso, o banco não consegue garantir por si só
  que um carro tenha número de portas, e a tabela acumula colunas vazias à
  medida que surgem novos tipos.

## Próximos passos

- Migrações versionadas com Flyway e perfis `dev`/`test`
- Clientes, funcionários e aluguéis (relacionamentos e transações)
- Testes de serviço com Mockito e de repositório com Testcontainers
- Autenticação JWT com papéis (ADMIN, EMPLEADO, CLIENTE)
- Swagger, Dockerfile, deploy em nuvem e CI no GitHub Actions
