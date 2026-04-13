package it.yourcompany.topologyservice;

import it.yourcompany.topologyservice.domain.result.NodeRef;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sanity tests for core domain value objects.
 * No Spring context required — these run fast without any external dependencies.
 */
class NodeRefTest {

    @Test
    void nodeRef_accessors_returnCorrectValues() {
        var ref = new NodeRef("svc-auth", "auth-service");
        assertThat(ref.id()).isEqualTo("svc-auth");
        assertThat(ref.name()).isEqualTo("auth-service");
    }

    @Test
    void nodeRef_equality_isByValue() {
        var a = new NodeRef("id-1", "name-1");
        var b = new NodeRef("id-1", "name-1");
        assertThat(a).isEqualTo(b);
    }
}
