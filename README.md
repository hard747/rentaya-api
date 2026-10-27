# RentaYa API

[![CI](https://github.com/hard747/rentaya-api/actions/workflows/ci.yml/badge.svg)](https://github.com/hard747/rentaya-api/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)

API REST para a gestão de uma concessionária de veículos em São Paulo: frota
(carros, motos e caminhões), clientes, aluguéis, funcionários, oficina
terceirizada, folha de pagamento mensal e avaliações.

## Stack

| Camada | Tecnologias |
|---|---|
| API | Java 21, Spring Boot 4 (Web MVC, Data JPA, Validation, Actuator) |
| Banco | PostgreSQL (Neon) com migrações versionadas no **Flyway** |
| Segurança | Spring Security, **JWT** (HMAC-SHA256), senhas com **BCrypt**, papéis ADMIN / EMPLEADO / CLIENTE |
| Documentação | OpenAPI 3 + **Swagger UI** (springdoc) |
| Testes | JUnit 5, **Mockito**, MockMvc, `@WebMvcTest`, `@DataJpaTest`, **Testcontainers** (PostgreSQL), H2 |
| Entrega | **Docker** multi-stage, **GitHub Actions** (CI), Blueprint do **Render** |

## Arquitetura

```
controller  →  service  →  repository  →  PostgreSQL (Flyway)
    ↑              ↑             ↑
  DTOs     regras de negócio   JPA: SINGLE_TABLE (Vehiculo)
 + JWT      no modelo de domínio      JOINED (Persona)
```

**Modelo de domínio** (o exercício de POO que originou o projeto):

| Tipo | Papel |
|---|---|
| `Alquilable`, `Pagable`, `Calificable` | interfaces |
| `Vehiculo`, `Persona`, `Empleado` | classes abstratas |
| `Auto`, `Moto`, `Camion`, `Cliente`, `ClientePremium`, `Conductor`, `Mecanico`, `TallerExterno` | classes concretas |

- Só carros e motos são `Alquilable`; caminhões são de uso interno.
- `ClientePremium` reutiliza o cálculo de `Cliente` (`super.calcularTotal`) e aplica o desconto.
- Empregados e oficina são `Pagable`: a folha do mês percorre um `List<Pagable>` sem saber o tipo de cada um.
- Motoristas e oficina são `Calificable`; mecânicos não.
- `Persona.mostrarInfo()` é escrito uma única vez e funciona para todos os tipos graças a `getRol()`.

**Banco de dados:** `vehiculos` usa herança `SINGLE_TABLE` e `personas` usa
`JOINED` (`clientes`, `clientes_premium`, `empleados`, `conductores`,
`mecanicos`). `alquileres` liga cliente e veículo com chaves estrangeiras, e as
regras críticas também são garantidas no banco com restrições `CHECK`.

## Endpoints

Base: `/api/v1` · documentação interativa em **`/swagger-ui.html`**.

| Recurso | Endpoints principais | Acesso |
|---|---|---|
| Autenticação | `POST /auth/login`, `POST /auth/registro` | público |
| Veículos | `GET /vehiculos` (paginado, `?tipo=AUTO`), `GET /vehiculos/{id}` | público |
| | `POST`, `PUT /{id}/tarifa`, `DELETE /{id}` (exclusão lógica), `GET /estadisticas/por-marca` | ADMIN |
| | `POST /{id}/kilometros`, `GET /{id}/mantenimiento` | ADMIN, EMPLEADO |
| Clientes | `POST /clientes` (`CLIENTE` ou `CLIENTE_PREMIUM`), `GET` (paginado) | ADMIN, EMPLEADO |
| Aluguéis | `POST /alquileres`, `POST /{id}/devolucion`, `GET /cotizacion`, `GET` (paginado) | todos; o CLIENTE só os seus |
| Funcionários | `POST /empleados` (`CONDUCTOR` ou `MECANICO`), `POST /{id}/viajes`, `POST /{id}/horas-extra`, `GET /{id}/revisiones/{vehiculoId}`, `GET /{id}/pago` | ADMIN (operação: EMPLEADO) |
| Avaliações | `POST /empleados/{id}/calificaciones` | ADMIN, CLIENTE |
| Oficina | `POST /talleres`, `POST /{id}/facturas`, `POST /{id}/calificaciones`, `GET /{id}/pago` | ADMIN |
| Folha | `GET /pagos/pendientes`, `POST /pagos/liquidaciones/{AAAA-MM}`, `GET /pagos/liquidaciones/{AAAA-MM}` | ADMIN |
| Usuários | `POST /usuarios` (ADMIN ou EMPLEADO), `GET /usuarios` | ADMIN |

### Regras de negócio

| Regra | Fórmula |
|---|---|
| Aluguel de carro | dias × tarifa; a partir de 7 dias, 10% de desconto |
| Aluguel de moto | dias × tarifa × 0,8; com capacete, + 5 por dia |
| Cliente premium | total do cliente normal − desconto (0 a 30%) |
| Manutenção | carro 100 + km × 0,05 · moto 50 + km × 0,03 · caminhão 300 + km × 0,10 + toneladas × 20 |
| Bônus por antiguidade | salário × 0,02 × anos |
| Pagamento | motorista: salário + bônus + viagens × 25 · mecânico: salário + bônus + horas extras × 30 · oficina: faturado × 1,18 |

Alugar e devolver mudam o estado do veículo e do aluguel na **mesma
transação**; liquidar a folha registra todos os pagamentos e zera os contadores
do mês, também numa única transação.

### Exemplo

```bash
# 1. Login
curl -X POST localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"<senha>"}'

# 2. Alugar um carro com o token recebido
curl -X POST localhost:8080/api/v1/alquileres \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{"clienteId":1,"vehiculoId":2,"dias":7}'
```

### Erros

Todos os erros têm o mesmo formato:

```json
{
  "timestamp": "2026-10-26T17:43:12",
  "status": 409,
  "error": "Conflict",
  "message": "El auto ABC-123 ya está alquilado",
  "path": "/api/v1/alquileres"
}
```

| Situação | Status |
|---|---|
| Dados inválidos, tipo inexistente, regra violada (ex.: alugar um caminhão) | 400 |
| Sem token, token inválido ou senha incorreta | 401 |
| Papel sem permissão, ou cliente acessando dados de outro cliente | 403 |
| Recurso não encontrado | 404 |
| Placa, DNI, e-mail ou RUC repetido; veículo já alugado; mês já liquidado | 409 |

## Como executar

1. Crie um `.env` na raiz a partir do `.env.example`:
   ```
   DB_URL=jdbc:postgresql://<host>/<database>?sslmode=require
   DB_USER=<usuario>
   DB_PASSWORD=<senha>
   JWT_SECRET=<pelo menos 32 caracteres aleatórios>
   ADMIN_USERNAME=admin
   ADMIN_PASSWORD=<senha-do-admin>
   ```
2. Rode a aplicação (perfil `dev`). O Flyway aplica as migrações e o
   administrador é criado no primeiro arranque:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Acesse `http://localhost:8080/swagger-ui.html`, faça login e use o botão
   **Authorize** com o token.

Com Docker:

```bash
docker build -t rentaya-api .
docker run -p 8080:8080 --env-file .env rentaya-api
```

### Perfis

| Perfil | Uso |
|---|---|
| `dev` (padrão) | Neon, credenciais do `.env`, SQL no log |
| `prod` | tudo por variáveis de ambiente, porta `PORT` |
| testes | H2 em memória ou PostgreSQL via Testcontainers; nunca o banco real |

## Testes

```bash
./mvnw verify
```

| Camada | Classe | O que verifica |
|---|---|---|
| Domínio | `EjercicioRentaYaTest` | o exercício de POO completo, com os valores esperados do enunciado |
| Serviço | `AlquilerServiceTest` | regras de aluguel com repositórios simulados (Mockito) |
| Web | `VehiculoControllerWebMvcTest` | validação, JSON e erros com o serviço simulado |
| Integração | `VehiculoControllerTest`, `FlujoRentaYaApiTest` | os cenários da API ponta a ponta, incluindo a folha mensal |
| Segurança | `SeguridadApiTest` | login, registro, tokens reais e permissões por papel |
| Repositório | `RepositoriosPostgresTest` | migrações, consultas nativas e `CHECK` sobre PostgreSQL real (Testcontainers) |

Os testes com Testcontainers precisam de Docker; sem ele, são ignorados
localmente e rodam no **GitHub Actions** a cada push.

A pasta `postman/` contém a coleção (34 requisições com testes) e o ambiente:

```bash
postman collection run postman/rentaya-api.postman_collection.json \
  -e postman/rentaya-local.postman_environment.json --env-var adminPassword=<senha>
```

## Deploy

O `render.yaml` descreve o serviço no **Render** (Docker, região Ohio, a mesma
do banco Neon). Em *New → Blueprint*, conecte este repositório e preencha
`DB_URL`, `DB_USER`, `DB_PASSWORD`, `ADMIN_USERNAME` e `ADMIN_PASSWORD`; o
`JWT_SECRET` é gerado automaticamente. O health check usa `/actuator/health`.

## Decisões de projeto

**Por que DTOs em vez de expor as entidades?**
Os DTOs separam o contrato da API do modelo de banco. A entrada aceita só os
campos que o cliente pode enviar (ele não define `id`, `kilometraje` nem
`activo`), as validações ficam no ponto de entrada e cada tipo tem seu próprio
formato. Na saída, a API devolve campos calculados (`descripcion`, `pagoMes`,
`promedioCalificacion`) sem expor detalhes do JPA, e o banco pode mudar sem
quebrar quem consome a API.

**Herança `SINGLE_TABLE` em veículos: vantagem e desvantagem**
- **Vantagem:** uma única tabela, sem `JOIN`; listagens e consultas
  polimórficas (todos os veículos de uma marca) são simples e rápidas.
- **Desvantagem:** as colunas próprias de cada subtipo (`numero_puertas`,
  `cilindrada`, `capacidad_toneladas`) precisam aceitar `NULL`, então o banco
  sozinho não garante que um carro tenha número de portas.

**Por que `JOINED` em pessoas?** Os subtipos têm muitas colunas próprias e
regras diferentes (descontos, salários, licenças). Com `JOINED`, cada tabela
guarda só as suas colunas, todas `NOT NULL` e com `CHECK`, e a integridade fica
no banco; o custo é um `JOIN` ao ler um subtipo.
