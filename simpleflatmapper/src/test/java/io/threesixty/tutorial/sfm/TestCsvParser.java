package io.threesixty.tutorial.sfm;

import org.junit.Test;
import org.simpleflatmapper.csv.CsvParser;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class TestCsvParser {
    static class Attribute {
        private String name;
        private String value;
        public String getName() { return name; }
        public String getValue() { return value; }
        public void setName(String name) { this.name = name; }
        public void setValue(String value) { this.value = value; }
        public Attribute(String name, String value) { this.name = name; this.value = value; }
        static Attribute build(Map.Entry<String, String> entry) { return new Attribute(entry.getKey(), entry.getValue()); }
        public String toString() { return name + "=" + value; }
        public boolean isNamed(final String name) { return this.name.equals(name); }
    }

    static class Bar {
        private Set<Attribute> attributes = new HashSet<>();

        public Bar(final Map<String, String> attrs) {
            attrs.entrySet().stream().map(Attribute::build).forEach(attributes::add);
        }

        public Optional<Attribute> get(final String name) {
            return this.attributes.stream().filter(attribute -> attribute.isNamed(name)).findFirst();
        }

        public String toString() {
            return attributes.toString();
        }
    }

    @Test
    public void testComplex() throws Exception {
        /* ID,VAL01,VAL02,VAL03
         * 10,20,30,40
         * 20,70,80,90 */
        File file = new File(TestCsvParser.class.getResource("/sample.csv").toURI());
        Function<Stream<Map>, Stream<Bar>> convertToBar = streamA -> streamA.map(TestCsvParser::build);

        CsvParser
                .mapTo(Map.class)
                .stream(file, convertToBar)
                .forEach(System.out::println);
    }

    public static Bar build(final Map<String, String> values) {
        return new Bar(values);
    }
}
