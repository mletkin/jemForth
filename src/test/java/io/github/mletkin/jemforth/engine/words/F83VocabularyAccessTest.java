package io.github.mletkin.jemforth.engine.words;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import io.github.mletkin.jemforth.engine.MemoryMapper;
import io.github.mletkin.jemforth.engine.exception.IllegalVocabularyAccess;

class F83VocabularyAccessTest {

    MemoryMapper mm = new MemoryMapper(5);

    @Test
    void firstVocabularyIsCurrentAndContext() {
        Map<Integer, Word> map = mkList(mkVoc("forth", 1));
        F83VocabularyAccess f83 = new F83VocabularyAccess(map::get);
        map.values().forEach(f83::add);

        SoftAssertions.assertSoftly(bunch -> {
            bunch.assertThat(f83.getCurrent()).isEqualTo(1);
            bunch.assertThat(f83.getContext()).isEqualTo(1);
        });
    }

    @Test
    void additionalVocabularyWillNotAffectCurrentOrContext() {
        Map<Integer, Word> map = mkList(mkVoc("forth", 1), mkVoc("further", 2));
        F83VocabularyAccess f83 = new F83VocabularyAccess(map::get);
        map.values().forEach(f83::add);

        SoftAssertions.assertSoftly(bunch -> {
            bunch.assertThat(f83.getCurrent()).isEqualTo(1);
            bunch.assertThat(f83.getContext()).isEqualTo(1);
        });
    }

    @Test
    void newWordsAreAddedToCurrentVocabulary() {
        VocabularyWord voc = mkVoc("forth", 1);
        Word word = mkWord("one", 2);
        Map<Integer, Word> map = mkList(voc, word);

        F83VocabularyAccess f83 = new F83VocabularyAccess(map::get);
        map.values().forEach(f83::add);

        assertThat(voc.find("one")).isSameAs(word);
    }

    @Test
    void wordIsFoundInContext() {
        VocabularyWord voc = mkVoc("further", 2);
        Map<Integer, Word> map = mkList(mkVoc("forth", 1), voc);
        F83VocabularyAccess f83 = new F83VocabularyAccess(map::get);
        map.values().forEach(f83::add);
        f83.setContext(2);
        voc.add(mkWord("bla", 5));

        assertThat(f83.find("bla").xt()).isEqualTo(5);
    }

    @Test
    void wordIsFoundInDefaultVocabulary() {
        VocabularyWord forth = mkVoc("forth", 1);
        VocabularyWord voc = mkVoc("further", 2);
        Map<Integer, Word> map = mkList(forth, voc);
        F83VocabularyAccess f83 = new F83VocabularyAccess(map::get);
        map.values().forEach(f83::add);
        f83.setContext(2);
        forth.add(mkWord("fasel", 6));

        assertThat(f83.find("fasel").xt()).isEqualTo(6);
    }

    @Test
    void wordIsNotFoundIfNeitherInDefaulNorContext() {
        VocabularyWord forth = mkVoc("forth", 1);
        VocabularyWord second = mkVoc("further", 2);
        VocabularyWord third = mkVoc("third", 3);
        Map<Integer, Word> map = mkList(forth, second, third);
        F83VocabularyAccess f83 = new F83VocabularyAccess(map::get);
        map.values().forEach(f83::add);
        f83.setContext(3);
        second.add(mkWord("fubar", 6));

        assertThat(f83.find("fubar")).isNull();
    }

    @Test
    void newWordsAreAddedToCurrent() {
        VocabularyWord further = mkVoc("further", 2);
        Map<Integer, Word> map = mkList(mkVoc("forth", 1), further);
        F83VocabularyAccess f83 = new F83VocabularyAccess(map::get);
        map.values().forEach(f83::add);

        f83.setCurrent(further.xt());
        f83.add(mkWord("word", 10));

        SoftAssertions.assertSoftly(bunch -> {
            bunch.assertThat(further.find("word").xt()).isEqualTo(10);
        });
    }

    @Test
    void forgetElimintesWordFromVocabulary() {
        VocabularyWord voc = mkVoc("forth", 1);
        Word word = mkWord("one", 2);
        Map<Integer, Word> map = mkList(voc, word);

        F83VocabularyAccess f83 = new F83VocabularyAccess(map::get);
        map.values().forEach(f83::add);

        f83.forgetWord(word);
        assertThat(voc.find("one")).isNull();
    }

    @Test
    void forgettingCurrentSetsDefaultVocabularyAsCurrent() {
        VocabularyWord forth = mkVoc("forth", 1);
        VocabularyWord voc = mkVoc("further", 2);
        Map<Integer, Word> map = mkList(forth, voc);
        F83VocabularyAccess f83 = new F83VocabularyAccess(map::get);
        map.values().forEach(f83::add);
        f83.setCurrent(2);

        f83.forgetWord(voc);

        assertThat(f83.getCurrent()).isEqualTo(1);
    }

    @Test
    void forgettingContextSetsDefaultVocabularyAsContext() {
        VocabularyWord forth = mkVoc("forth", 1);
        VocabularyWord voc = mkVoc("further", 2);
        Map<Integer, Word> map = mkList(forth, voc);
        F83VocabularyAccess f83 = new F83VocabularyAccess(map::get);
        map.values().forEach(f83::add);
        f83.setContext(2);

        f83.forgetWord(voc);

        assertThat(f83.getContext()).isEqualTo(1);
    }

    @Test
    void settingCurrentToUnknownVocabularyThrowsException() {
        F83VocabularyAccess f83 = new F83VocabularyAccess(t -> null);
        assertThatExceptionOfType(IllegalVocabularyAccess.class).isThrownBy(() -> f83.setCurrent(5));
    }

    @Test
    void settingContextToUnknownVocabularyThrowsException() {
        F83VocabularyAccess f83 = new F83VocabularyAccess(t -> null);
        assertThatExceptionOfType(IllegalVocabularyAccess.class).isThrownBy(() -> f83.setContext(5));
    }

    private VocabularyWord mkVoc(String name, int xt) {
        return allocate(new VocabularyWord(name), xt);
    }

    private Word mkWord(String name, int xt) {
        return allocate(new Word(name), xt);
    }

    private <T extends Word> T allocate(T word, int xt) {
        word.useMemoryMapper(mm);
        word.setXt(xt);
        return word;
    }

    private Map<Integer, Word> mkList(Word... list) {
        return Stream.of(list).collect(Collectors.toMap(Word::xt, w -> w));
    }

}
