/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Variable storing a single cell content that may be linked to an external value container.
 * <p>
 * User variables should be considered as global, although they are usually bound to the eingine.<br>
 * There should be special engine-specific variable words too.
 *
 * TODO: throw ex on access pfa != xt + MemoryMapper.CELL_SIZE
 */
public class UserVariableWord extends Word {

    Supplier<Integer> getter;
    Consumer<Integer> setter;

    public UserVariableWord(String name, Supplier<Integer> getter, Consumer<Integer> setter) {
        this.name = name;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public Integer fetch(int pfa) {
        return getter.get();
    }

    @Override
    public void store(int pfa, Integer value) {
        setter.accept(value);
    }

}
