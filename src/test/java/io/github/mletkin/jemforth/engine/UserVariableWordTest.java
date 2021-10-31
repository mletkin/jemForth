package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserVariableWordTest {

    @Test
    void executePushesPfa() {
        UserVariableWord word = new UserVariableWord("foobar", () -> 5, y -> {});
        word.xt = 10;
        JemEngine engine = new JemEngine();
        word.execute(engine);
        assertThat(engine.getDataStack()).contains(14);
    }

    @Test
    void nameParameterSetsTheName() {
        UserVariableWord word = new UserVariableWord("foobar", () -> 5, y -> {});
        assertThat(word.name()).isEqualTo("foobar");
    }

    @Test
    void storeSetsTheValue() {
        Store<Integer> store = new Store<>();
        UserVariableWord word = new UserVariableWord("foobar", store::get, store::set);

        word.store(4711, 15);
        assertThat(store.get()).isEqualTo(15);
    }

    @Test
    void fetchGetsTheValue() {
        Store<Integer> store = new Store<>();
        UserVariableWord word = new UserVariableWord("foobar", store::get, store::set);
        store.set(16);

        assertThat(word.fetch(4852)).isEqualTo(16);
    }

    @Test
    void cfaParameterSetsTheCfa() {
        Store<Integer> store = new Store<>();
        InternalWord word = new InternalWord("foobar", e -> store.set(10));
        JemEngine engine = new JemEngine();

        word.execute(engine);

        assertThat(store.get()).isEqualTo(10);
    }

}
