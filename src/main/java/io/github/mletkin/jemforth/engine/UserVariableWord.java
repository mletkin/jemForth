/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A variable storing a single cell content that may be linked to an external
 * value container.
 * <p>
 * User variables should be considered as global, although they are usually
 * bound to the engine.<br>
 * There should be special engine-specific variable words too.<br>
 * A variable may be made read only or write only by setting one of the lambda
 * expressions appropriately.
 *
 * TODO: throw ex on access pfa != xt + MemoryMapper.CELL_SIZE
 */
public class UserVariableWord extends Word {

    /**
     * hook for retrieving the content.
     */
    private final Supplier<Integer> getter;

    /**
     * hook for changing the content.
     */
    private final Consumer<Integer> setter;

    /**
     * Creates a new user variable word.
     *
     * @param name
     *                   the name of the word
     * @param getter
     *                   a lambda expression to retrieve the value
     * @param setter
     *                   a lambda expression to change the value
     */
    public UserVariableWord(String name, Supplier<Integer> getter, Consumer<Integer> setter) {
        super(name);
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
