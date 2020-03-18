# Bem Vindos ao PlayGround

Projeto para implementação de novas tecnologias e ferramentas, escrito em Java + Spring Boot.

[1.ArchUnit](#archunit)

[1.Testes de Integração](#Teste)



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
[GITHUB](https://github.com/LeonardoAndriotti/playground/blob/master/src/test/java/br/com/playground/archunit/ArchUnitTest.java)

## Testes de Integração

Os testes de integração se concentram na integração entre diferentes camadas do aplicativo. Utilizando o contexto semelhante ao de produção.

No entanto testes de integrações podem ser demorados, pois vão iniciar o contexto do Spring e banco de dados para testar aplicação. Por esse motivo vamos separar nossos testes por profiles.

### Configurando dependências para criar os testes: 

``` maven

   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-test</artifactId>
       <scope>test</scope>
       <version>2.1.6.RELEASE</version>
   </dependency>
   <dependency>
       <groupId>com.h2database</groupId>
       <artifactId>h2</artifactId>
       <scope>test</scope>
       <version>1.4.194</version>
   </dependency>
	    
```

### Configurando Banco de Dados

Para executar os testes vamos utilizar um properties separado: **application-teste.properties**, nele coloque as seguintes configurações:

spring.datasource.url = jdbc:h2:mem:test

spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.H2Dialect

### Explicando as Anotações

#### @SpringBootTest
Pode ser usada quando precisamos inicializar o contâiner inteiro. A anotação funciona criando o ApplicationContext que será utilizado.

#### WebEnviroment

Usamos webEnviroment para atribuir para configurar o ambiente de tempo de execução. 

##### Atribuições

**MOCK**
Cria um WebApplicationContextambiente de servlet simulado, se as APIs de servlet estiverem no caminho de classe, e ReactiveWebApplicationContextse o Spring WebFlux estiver no caminho de classe ou de ApplicationContext outra forma regular .

**RANDOM_PORT**
Cria um contexto de aplicativo da Web (reativo ou baseado em servlet) e define uma server.port=0 Environmentpropriedade (que geralmente aciona a escuta em uma porta aleatória). Geralmente usado em conjunto com um @LocalServerPortcampo injetado no teste.

**DEFINED_PORT**
Cria um contexto de aplicativo da Web (reativo) sem definir nenhuma server.port=0 Environmentpropriedade.

**NONE**
Cria um ApplicationContexte define SpringApplication.setWebApplicationType(WebApplicationType)como WebApplicationType.NONE.


#### @TestPropertySource
Pode ser utilizado para definir qual arquivo properties utilizar, aqui vamos utilizar nosso properties de teste: application-teste.properties

#### @TestInstance 
É uma anotação em nível de tipo usada para configurar o ciclo de vida das instâncias de teste para a classe de teste anotada ou a interface de teste.

**LifeCycle.PER_CLASS**  nos permite pedir ao JUnit para criar apenas uma instância da classe de teste e reutilizá-la entre os testes.

#### @IntegrationTest
Está é uma anotação criada para identificar o tipo do testes, ela nada mais é que uma interface, anotada com um @Tag do Junit 5, para identificar o tipo do teste.

### Criando Anotação


``` java

@Tag("IntegrationTest")
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationTest {
}


```

### Criando nossos testes

No testes estou utilizando o MockMvc para fazer consultas e também RestTemplate, por questão de exemplos.

Note que antes dos testes serem executados estou, inserindo uma configuração no banco de dados e em sequencia executa os testes.

``` java

@IntegrationTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations = "classpath:application-teste.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = PlaygroundTestApplication.class)
public class PlaygroundAPIIntegrationTest {

    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static final Gson GSON = new GsonBuilder().setDateFormat(ISO_DATE_FORMAT).create();

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ConfigurationRepository repository;

    @BeforeEach
    public void beforeClass() throws Exception {
        Configuration configuration = new Configuration();
        configuration.setPlaygroundUser("teste3");
        repository.save(configuration);
    }

    @Test
    public void getConfigurations() throws Exception {
        mvc.perform(get("/api/configurations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect((ResultMatcher) jsonPath("$[0].playgroundUser", is("teste3")));

    }

    @Test
    public void getConfigurationById() {
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("id", "1");

        String result = restTemplate.exchange("http://localhost:7792/api/configuration/{id}", HttpMethod.GET, new HttpEntity(getHeaders()), String.class, urlParams).getBody();
        Configuration configuration = GSON.fromJson(result, Configuration.class);

        assertEquals("teste3", configuration.getPlaygroundUser());

    }

    @Test
    public void saveConfiguration() throws Exception {
        Configuration configuration = new Configuration();
        configuration.setPlaygroundUser("Integration Test");

        String json = mapper.writeValueAsString(configuration);

        mvc.perform(post("/api/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.playgroundUser", is("Integration Test")));
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("oi", "47.");
        headers.set("accept-language", "pt");
        return headers;
    }
}


```

### Configurando profiles de testes

* Crie um novo profile

``` maven

        <profile>
            <id>AllTests</id>
            <properties>
                <excludeTags>none</excludeTags>
            </properties>
        </profile>


```

* Em seguida o pluguin do maven surefire 


``` maven

			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-surefire-plugin</artifactId>
				<version>2.22.1</version>
				<configuration>
					<properties>
						<excludeTags>${excludeTags}</excludeTags>
					</properties>
				</configuration>
				<dependencies>
					<dependency>
						<groupId>org.junit.platform</groupId>
						<artifactId>junit-platform-surefire-provider</artifactId>
						<version>1.3.2</version>
					</dependency>
					<dependency>
						<groupId>org.junit.jupiter</groupId>
						<artifactId>junit-jupiter-engine</artifactId>
						<version>5.5.2</version>
					</dependency>
				</dependencies>
			</plugin>

```

* E no properties do pom 

``` maven

<excludeTags>IntegrationTest</excludeTags>

```

Com isso seus testes de integração só vão rodar quando executar o clean install utilizando -PAllTests
