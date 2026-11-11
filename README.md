# MedPro

API REST de clínica médica desenvolvida nas aulas de Aplicações WEB.

## Conteúdo implementado

A implementação acompanha o material **API Rest – Parte 2**, até o exercício 4:

- Entidades JPA `Medico`, `Paciente` e endereço embutido `Endereco`.
- Persistência com Spring Data JPA e MySQL.
- Migrations Flyway para criação das tabelas, telefone e situação ativo/inativo.
- Cadastro validado de médicos e pacientes, incluindo o endereço.
- Listagem com DTOs, paginação e ordenação; somente registros ativos são retornados.
- Atualização de nome, telefone e endereço. Campos omitidos ou nulos são preservados.
- Exclusão lógica: os dados continuam no banco com `ativo = false`.
- Respostas para dados inválidos (400), ID inexistente (404) e cadastro duplicado (409).

O agendamento e o cancelamento de consultas são objetivos futuros do projeto; o PDF desta etapa termina nas operações de médicos e pacientes.

Os nomes existentes `DadosCadastroMedicos` e `DadosCadastroPacientes` foram mantidos. Os exemplos das aulas usam esses nomes no singular.

## Executar no Windows / PowerShell

Requisitos: JDK 25 e MySQL em execução. O Maven Wrapper está no repositório.

Crie o banco uma única vez no MySQL Workbench ou cliente SQL:

```sql
CREATE DATABASE medpro_api CHARACTER SET utf8mb4;
```

Na pasta do projeto, configure as credenciais e inicie a API:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "SUA_SENHA_DO_MYSQL"
.\mvnw.cmd spring-boot:run
```

Por padrão, a conexão é `jdbc:mysql://localhost:3306/medpro_api`. Se necessário, altere apenas a variável `DB_URL`:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/medpro_api"
```

No Linux/macOS: `DB_USERNAME=root DB_PASSWORD=sua_senha bash mvnw spring-boot:run`.

O Flyway aplica automaticamente os scripts em `src/main/resources/db/migration`. Pare a aplicação antes de adicionar novas migrations; não edite scripts que já foram aplicados ao seu banco. A estrutura é gerenciada pelo Flyway, sem criação automática de tabelas pelo Hibernate.

## Rotas

| Método | URL | Comportamento |
| --- | --- | --- |
| POST | `/medicos` | Cadastra um médico e devolve ID e resumo (201) |
| GET | `/medicos` | Lista médicos ativos com paginação (200) |
| PUT | `/medicos` | Atualiza o médico identificado pelo `id` do JSON (200) |
| DELETE | `/medicos/{id}` | Inativa o médico (204) |
| POST | `/pacientes` | Cadastra um paciente e devolve ID e resumo (201) |
| GET | `/pacientes` | Lista pacientes ativos com paginação (200) |
| PUT | `/pacientes` | Atualiza o paciente identificado pelo `id` do JSON (200) |
| DELETE | `/pacientes/{id}` | Inativa o paciente (204) |

A listagem padrão retorna 10 registros por página, ordenados por nome. Exemplos:

```text
http://localhost:8080/medicos?size=1&page=0&sort=nome,desc
http://localhost:8080/pacientes?size=10&page=1&sort=nome
```

`page` começa em zero. A resposta contém `content`, `totalElements`, `totalPages` e os demais dados de paginação. A listagem de médicos contém ID, nome, email, CRM e especialidade; a de pacientes contém ID, nome, email e CPF.

### Cadastro de médico

Envie para `POST http://localhost:8080/medicos`, com `Content-Type: application/json`:

```json
{
  "nome": "Ana Silva",
  "email": "ana@example.com",
  "crm": "123456",
  "telefone": "16999999999",
  "especialidade": "ORTOPEDIA",
  "endereco": {
    "logradouro": "Rua Exemplo",
    "bairro": "Centro",
    "cep": "13574000",
    "cidade": "São Carlos",
    "uf": "SP",
    "numero": "10",
    "complemento": "Sala 2"
  }
}
```

Especialidades: `CARDIOLOGIA`, `DERMATOLOGIA`, `GINECOLOGIA` e `ORTOPEDIA`. CRM: 4 a 6 dígitos. CEP: 8 dígitos sem hífen. Número e complemento do endereço são opcionais.

### Cadastro de paciente

Envie para `POST http://localhost:8080/pacientes`:

```json
{
  "nome": "João Silva",
  "email": "joao@example.com",
  "cpf": "12345678901",
  "telefone": "16988888888",
  "endereco": {
    "logradouro": "Rua Exemplo",
    "bairro": "Centro",
    "cep": "13574000",
    "cidade": "São Carlos",
    "uf": "SP",
    "numero": "20"
  }
}
```

CPF: 11 dígitos sem pontuação (validação de formato, sem cálculo dos dígitos verificadores). Email e CPF são únicos entre pacientes; email e CRM são únicos entre médicos.

### Atualização parcial

Envie para `PUT http://localhost:8080/medicos` ou `/pacientes`, usando o ID retornado no cadastro:

```json
{
  "id": 1,
  "nome": "Nome atualizado",
  "telefone": "16977777777",
  "endereco": {
    "numero": "42"
  }
}
```

O `id` é obrigatório. Os demais campos são opcionais. Email, CRM, especialidade e CPF não são alterados pelo PUT. Campos obrigatórios não aceitam texto em branco; número e complemento aceitam `""` para limpar o valor.

### Exclusão lógica

Envie `DELETE http://localhost:8080/medicos/1` ou `/pacientes/1`, substituindo `1` pelo ID real. A resposta 204 não contém corpo. O registro permanece no banco e sai da listagem de ativos.

## Testes

```powershell
.\mvnw.cmd test
```

Os testes usam H2 em modo MySQL, sem acessar nem modificar seu banco local. Executam as quatro migrations Flyway e verificam cadastro, validação, duplicidade, paginação, ordenação, atualização parcial, rollback e exclusão lógica para os dois recursos. O H2 ajuda a verificar o fluxo da aplicação, mas não substitui uma validação em uma instalação real do MySQL.
