# Veterinary Clinic — Revitalização de Sistema Legado

> Trabalho prático da disciplina **Manutenção de Software** — Engenharia de Software, 5º Período  
> Universidade CESUMAR | Professora: Jéssica Bueno  
> **Equipe:** Fernanda Gandolfi · Kimberly Kelly · Matheus Vian

---

## Sobre o projeto

Sistema desktop de agendamento de consultas veterinárias, originalmente desenvolvido como material didático por [RitanMihai](https://github.com/RitanMihai/Veterinary-Clinic).

Este repositório documenta o processo de **reengenharia incremental** aplicado ao sistema legado ao longo dos checkpoints da disciplina, com foco na eliminação de dívidas técnicas e na modernização da arquitetura.

![Formulário de Agendamento](docs/images/form_filled_ss.png)

---

## Stack tecnológica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 11 LTS |
| Interface gráfica | JavaFX 17 / FXML |
| Persistência (ORM) | JPA / EclipseLink 2.7.7 |
| Banco de dados | PostgreSQL 17 |
| Build | Apache Maven 3.8+ |
| Controle de versão | Git + GitHub (GitFlow) |

---

## Pré-requisitos

Antes de rodar o projeto, instale:

- **Java 11+** → [adoptium.net](https://adoptium.net) ou [Microsoft OpenJDK](https://learn.microsoft.com/pt-br/java/openjdk/download)
- **Apache Maven 3.8+** → [maven.apache.org](https://maven.apache.org/download.cgi)
- **PostgreSQL 17** → [postgresql.org](https://www.postgresql.org/download/)

---

## Como rodar

### 1. Clonar o repositório

```bash
git clone https://github.com/MatheusStoco/Veterinary-Clinic.git
cd Veterinary-Clinic
```

### 2. Criar e popular o banco de dados

```bash
# Criar o banco
psql -U postgres -c "CREATE DATABASE veterinary_clinic;"

# Importar o schema e dados iniciais
psql -U postgres -d veterinary_clinic -f db_dump/veterinary_clinic.sql
```

### 3. Configurar a conexão

Edite o arquivo `form/src/main/resources/META-INF/persistence.xml` com as suas credenciais do PostgreSQL:

```xml
<property name="javax.persistence.jdbc.url"
          value="jdbc:postgresql://127.0.0.1:5432/veterinary_clinic?stringtype=unspecified"/>
<property name="javax.persistence.jdbc.user"     value="postgres"/>
<property name="javax.persistence.jdbc.password" value="SUA_SENHA"/>
```

### 4. Executar a aplicação

```bash
cd form
mvn javafx:run
```

> **Windows:** você pode usar o arquivo `rodar.bat` na raiz do projeto — ele configura o ambiente e inicia o banco automaticamente.

---

## Estrutura do projeto

```
Veterinary-Clinic/
├── db_dump/
│   └── veterinary_clinic.sql       # Schema + dados iniciais
├── docs/
│   ├── images/
│   ├── diagrama_classes.puml       # Diagrama de Classes UML
│   ├── diagrama_sequencia.puml     # Diagrama de Sequência UML
│   └── veterinary_clinic_diagram.puml
├── form/
│   └── src/main/java/
│       ├── Main.java                        # Entry point JavaFX
│       ├── service/
│       │   └── ClinicService.java           # Facade — camada de negócio
│       ├── database/
│       │   ├── DatabaseConnection.java      # Singleton — conexão JPA
│       │   ├── exception/                   # Hierarquia de exceções
│       │   │   ├── ClinicException.java
│       │   │   ├── PersistenceException.java
│       │   │   └── BusinessException.java
│       │   ├── dao/
│       │   │   ├── DaoI.java                # Interface genérica DAO
│       │   │   ├── AbstractDao.java         # Template Method — CRUD base
│       │   │   ├── AnimalDao.java
│       │   │   ├── AppointmentDao.java
│       │   │   ├── ClientDao.java
│       │   │   ├── MedicDao.java
│       │   │   ├── ScheduleDao.java
│       │   │   └── SurgeryDao.java
│       │   └── model/                       # Entidades JPA
│       └── gui/
│           ├── controllers/
│           │   ├── MainController.java      # Controller de UI (só apresentação)
│           │   └── SubmittedController.java
│           └── model/
│               └── Calendar.java            # Lógica de horários disponíveis
└── rodar.bat                                # Script de inicialização (Windows)
```

---

## Plano de Reengenharia — Checkpoint 03

As refatorações abaixo foram aplicadas com base nos **Code Smells** identificados na análise arquitetural (Checkpoint 02), seguindo o catálogo de Fowler (2018) e os padrões GoF.

| ID | Code Smell | Padrão Aplicado | Arquivo(s) |
|---|---|---|---|
| RF-01 | God Class / Feature Envy | **Facade** + Extract Class | `ClinicService.java`, `MainController.java` |
| RF-02 | Inappropriate Intimacy | **Singleton** | `DatabaseConnection.java` |
| RF-03 | Lazy Class / Dead Code | **Template Method** + AbstractDao | `AbstractDao.java`, todos os DAOs |
| RF-04 | Shotgun Surgery | Move Method | `ClinicService.java`, `Calendar.java` |
| RF-05 | Primitive Obsession | Replace Data Value with Object (`@ManyToOne`) | `AppointmentEntity.java` |
| RF-06 | Inappropriate Error Handling | Introduce Exception Hierarchy | `exception/`, `DatabaseConnection.java` |

---

## Banco de dados — modelo

```
animal          ← espécies e raças disponíveis
medic           ← veterinários cadastrados
schedule        ← horários de trabalho por dia da semana
surgery         ← tipos de procedimentos (nome, duração, preço)
client          ← criado no momento do agendamento
appointment     ← agendamento (liga client + animal + medic + surgery)
```

---

## Referências

- FOWLER, M. *Refactoring: improving the design of existing code*. 2. ed. Addison-Wesley, 2018.
- GAMMA, E. et al. *Design Patterns: elements of reusable object-oriented software*. Addison-Wesley, 1995.
- PRESSMAN, R. S.; MAXIM, B. R. *Engenharia de Software*. 9. ed. AMGH, 2021.
- Repositório original: [RitanMihai/Veterinary-Clinic](https://github.com/RitanMihai/Veterinary-Clinic)
