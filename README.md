# Bem Vindos ao PlayGround

Projeto para implementação de novas tecnologias e ferramentas, escrito em Java + Spring Boot.

1. ArchUnit





## ArchUnit

Vamos utilizar o archUnit, para validar nossa arquitetura. Para garantir que com o tempo não se perga os limites colocados, estrutura de pacotes e projeto vire aquela bagunça.

O archUnit é escrito em java, para testar uma estrutura java e você consegue utilizar ele com JUnit 4 e 5.

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
 Para declarar um teste utilizamos anotação @ArchTest e a classe ArchRule para uma regra sobre um conjuto especificado de objetos.

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

#### Verificar exceptions genéricas

Throwable, Exception, RuntimeException

``` java

   @ArchTest
    @PublicAPI(usage = ACCESS)
    public static final ArchRule NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS = noClasses()
            .should(THROW_GENERIC_EXCEPTIONS)
            .as("This class cannot throw general exceptions.");
	    
```

#### Validações personalizadas

O ArchUnit, lhe da a liberdade de criar suas próprias validaçes, vamos criar uma para validar se o pacode que começa com use, tem anotaçes controller.

``` java
  private static final DescribedPredicate PACKAGE_PREDICATE = new PackagePredicate();
    private static final ArchCondition NO_CONTROLLER_CLASS_CONDITION = new NoControllerClassCondition();


    @ArchTest
    public static final ArchRule O_CLASS_ANNOTATED_CONTROLLER_IN_USE_CASE =                classes().that(PACKAGE_PREDICATE).should(NO_CONTROLLER_CLASS_CONDITION);

```

``` java
/*
 * Responsavel pela regra do teste, condição para passar.
 * */
class NoControllerClassCondition extends ArchCondition {

    public NoControllerClassCondition() {
        super("not contain a method named foo");
    }


    @Override
    public void check(Object item, ConditionEvents events) {
        ((JavaClass) item).getAnnotations()
                .stream()
                .map(JavaAnnotation::getRawType)
                .filter(c -> c.getSimpleName().equals("Controller"))
                .forEach(c -> events
                        .add(SimpleConditionEvent
                                .violated(c, "class " + ((JavaClass) item).getSimpleName() + " contains a annotation controller")));
    }
}
```

``` java
/*
 *
 * Seleciona as classes que vão ser validadas.
 * */
class PackagePredicate extends DescribedPredicate {

    private static final String PACKAGE = "br.com.playground";

    public PackagePredicate() {
        super("resides in package " + PACKAGE);
    }

    @Override
    public boolean apply(Object javaClass) {
        return ((JavaClass) javaClass).getPackage().getRelativeName().startsWith("use");
    }
}


```
