package br.com.facilit.kanban.delivery.graphql;

import br.com.facilit.kanban.application.health.HealthQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;

@GraphQlTest(HealthGraphQlController.class)
@Import(HealthQuery.class)
class HealthGraphQlControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void exposesHealthOverGraphQl() {
        graphQlTester.document("{ health { status } }")
                .execute()
                .path("health.status")
                .entity(String.class)
                .isEqualTo("UP");
    }
}
