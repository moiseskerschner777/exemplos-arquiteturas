sistema sem arquitetura definida: seis casos de uso

    casos de uso, nos dois estados:

        - abrir conta
        - depositar
        - sacar
        - transferir
        - encerrar conta
        - consultar extrato, paginado

    estrutura:

        - legado: Java 8, um Servlet e um JSP por caso de uso, JDBC sobre Oracle
        - migrado: Java 17, um controlador REST por caso de uso, Spring Boot, JPA, PostgreSQL e componente Angular

    como construir e testar:

        - legado: mvn package gera o war para o Tomcat 9, mvn test usa Testcontainers com Oracle
        - migrado: mvn test usa Testcontainers com PostgreSQL
        - com Docker recente: mvn test -DargLine="-Dapi.version=1.44"

    banco do estado legado:

        docker compose -f legado/docker/docker-compose.yml up
        variáveis do Tomcat: DB_URL, DB_USER, DB_PASSWORD

    banco do estado migrado:

        docker compose -f migrado/docker-compose.yml up
        mvn spring-boot:run
