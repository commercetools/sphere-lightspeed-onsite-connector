package io.sphere.sdk.lightspeed.client;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class TestFoo {

    @Test
    public void testFoo() throws Exception {
        assertThat(new Foo().foo()).isEqualTo("foo");
    }
}
