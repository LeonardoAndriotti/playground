# Bem Vindos ao PlayGround

Projeto para implementação de novas tecnologias e ferramentas, escrito em Java + Spring Boot.

1. ArchUnit





## ArchUnit

Vamos utilizar o archUnit, para validar nossa arquitetura. Para garantir que com o tempo não se perga os limites colocados, estrutura de pacotes e projeto vire aquela bagunça.

O archUnit é escrito em java, para testar uma estrutura java e vocÇe consegue utilizar ele com JUnit 4 e 5.

###### Obs. Os exemplos serão utilizando Junit 5.

### Instalação

		<dependency>
			<groupId>com.tngtech.archunit</groupId>
			<artifactId>archunit-junit5</artifactId>
			<version>0.13.1</version>
			<scope>test</scope>
		</dependency>


### O que vamos testar

Vamos utilizar uma arquitetura simples, baseada em componentes.

Sendo o pacote **API** o pacote de nível mais alto e responsavel por receber as entradas do sistema. Em seguida temos o pacote **APPLICATION** que fica responsavel por regras da aplicação e por fim temos o pacote **USECASE** responsavel pela regra de negocio da aplicação.

###### Regras

1. O pacote API, não depende de nenhum outro pacote a não ser o application e somente ele pode conter os controllers. O pacote API também não pode ser dependencia de nenhum outro pacote.

1. O pacote APPLICATION só pode depender do pacote USECASE.

1. O pacote USECASE não pode depender de nenhum pacote.


### Conhecendo algumas anotações

Para especificar quais pacotes ler para este teste ( @AnalyzeClasses (packages = {..}) 
``` java
  @AnalyzeClasses(packages = "br.com.playground")
```
 Para declarar um teste utilizamos anotação @ArchTest e a classe ArchRule para uma regra.

### Criando os Testes

#### Verificações de camada

Validar quais pacotes podem ser chamados e por qual pacote pode chamar.

``` java

    @ArchTest
    private static final ArchRule LAYER_CHECKS = layeredArchitecture()
            .layer("API").definedBy("br.com.playground.api")
            .layer("APPLICATION").definedBy("br.com.playground.application")
            .layer("USECASE").definedBy("br.com.playground.usecase")

            .whereLayer("API").mayNotBeAccessedByAnyLayer()
            .whereLayer("APPLICATION").mayOnlyBeAccessedByLayers("API")
            .whereLayer("USECASE").mayOnlyBeAccessedByLayers("APPLICATION");

```

#### Proibindo anotaçãoes em pacotes

``` java
    @ArchTest
    private static final ArchRule NO_DEPRECATED_IN_USE_CASE_PACKAGE = noClasses().that()
            .areAnnotatedWith(Deprecated.class)
            .should()
            .resideInAPackage("br.com.playground..")
            .as("Deprecated annotation invalid,")
            .because("Deprecated classes are not allowed.");

    @ArchTest
    private static final ArchRule NO_CONTROLLER_IN_USE_CASE_PACKAGE = noClasses().that()
            .areAnnotatedWith(Controller.class)
            .or()
            .areAnnotatedWith(RestController.class)
            .should()
            .resideInAPackage("br.com.playground.usecase")
            .andShould()
            .resideInAPackage("br.com.playground.application")
            .as("Controller annotations are not allowed")
            .because("REST calls are not allowed in packages [application, usecase]");
```

#### Verificações de ciclos

``` java
    @ArchTest
    private static final ArchRule NO_CYCLIC_DEPENDENCIES = slices()
            .matching("com.playground.(*)..")
            .should()
            .beFreeOfCycles();
```	   
