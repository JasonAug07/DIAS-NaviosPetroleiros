# Sistema de Gestao de Navios Petroleiros

Aplicacao desktop em Java/JavaFX para apoio a gestao de uma frota de navios-tanque:
navios, tipos de navio e de carga, portos, viagens, cargas e tripulacao. Os dados sao
persistidos numa base de dados relacional Microsoft SQL Server.

Trabalho desenvolvido no ambito da unidade curricular de Desenvolvimento Iterativo e Agil
de Software (DIAS), ISEP, 2025/2026.

## Funcionalidades

- Gestao de navios: registo, edicao, consulta e alteracao de estado operacional
- Tipos de navio e de carga, com matriz de compatibilidade navio/carga
- Gestao de portos
- Gestao de viagens com ciclo de estados (Planeada, Em Curso, Concluida, Cancelada)
- Associacao de cargas e de tripulacao a viagens
- Controlo de disponibilidade de navios e de tripulantes
- Historico de viagens por tripulante
- Registo de eventos operacionais dos navios (log de auditoria)
- Pesquisas e filtros em todos os modulos

## Regras de negocio

- Um navio so pode ter uma viagem ativa de cada vez
- A capacidade do navio nao pode ser excedida pelo peso das cargas
- Tem de existir compatibilidade entre o tipo de navio e o tipo de carga
- Navios em manutencao ou inativos nao podem iniciar viagens
- As viagens evoluem apenas por transicoes de estado validas
- Um tripulante em viagem nao pode ser associado a outra viagem

As regras sao validadas em duas camadas: na aplicacao (camada de servicos) e na propria
base de dados (stored procedures, funcoes e triggers).

## Arquitetura

Arquitetura em camadas com separacao clara de responsabilidades:

```
model/       entidades de dominio e enums
dao/         acesso a dados (JDBC, stored procedures, funcoes, views)
service/     regras de negocio e validacao (BusinessException)
controller/  controladores JavaFX
resources/   ecrans FXML e folhas de estilo
```

Padroes de design utilizados: MVC, DAO, Service Layer e Singleton (ligacao a base de dados).

## Tecnologias

- Java 21
- JavaFX 21
- Microsoft SQL Server (driver mssql-jdbc)
- Maven
- JUnit 5 (testes unitarios)

## Pre-requisitos

- JDK 21 ou superior
- Microsoft SQL Server (instancia local, por omissao `localhost\SQLEXPRESS`)
- Maven, ou IntelliJ IDEA com Maven integrado

## Configuracao da base de dados

1. Criar a base de dados e os objetos executando os scripts SQL do projeto
   (`base_de_dados.sql` para as tabelas e `tbd_objetos.sql` para views, funcoes,
   procedimentos e triggers).
2. Criar o login da aplicacao com `setup_login_app.sql` (cria o login `petroleiros`).
3. Opcionalmente, popular com dados de exemplo (`povoamento_via_sp.sql`).
4. Confirmar a ligacao em
   `src/main/java/com/petroleiros/dao/DatabaseConnection.java`
   (servidor, base de dados `final_DIAS_TBD`, utilizador e palavra-passe).

Nota: o servico SQL Server Browser deve estar ativo para a ligacao por instancia nomeada
atraves de JDBC.

## Compilar e executar

Com Maven:

```
mvn clean javafx:run
```

Em alternativa, abrir o projeto no IntelliJ IDEA e executar a classe
`com.petroleiros.MainApp`.

## Testes

```
mvn test
```

Os testes unitarios cobrem a logica de dominio e as regras de negocio (navios, viagens,
tripulantes e conversao de enums).

## Estrutura do projeto

```
src/main/java/com/petroleiros/
  MainApp.java          ponto de entrada da aplicacao
  model/                entidades e enums
  dao/                  acesso a dados
  service/              regras de negocio
  controller/           controladores JavaFX
src/main/resources/views/   ecrans FXML e estilos
src/test/java/          testes JUnit
```

## Equipa

| Numero  | Membro | GitHub       |
|---------|--------|--------------|
| 1251704 | Victor | ippVictor    |
| 1251684 | Jason  | JasonAug07   |
| 1251948 | Marco  | Marco-dev06  |
