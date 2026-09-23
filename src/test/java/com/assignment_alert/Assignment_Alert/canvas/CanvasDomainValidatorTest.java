package com.assignment_alert.Assignment_Alert.canvas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.assignment_alert.Assignment_Alert.exceptions.RequestValidationException;

class CanvasDomainValidatorTest {

    private CanvasDomainValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CanvasDomainValidator();
        allowSuffixes(List.of());
    }

    private void allowSuffixes(List<String> suffixes) {
        ReflectionTestUtils.setField(validator, "allowedSuffixes", suffixes);
    }

    @Test
    void addsTheHttpsSchemeWhenItIsMissing() {
        assertThat(validator.normalize("8.8.8.8")).isEqualTo("https://8.8.8.8");
    }

    @Test
    void stripsTrailingSlashes() {
        assertThat(validator.normalize("8.8.8.8///")).isEqualTo("https://8.8.8.8");
    }

    @Test
    void trimsSurroundingWhitespace() {
        assertThat(validator.normalize("   8.8.8.8   ")).isEqualTo("https://8.8.8.8");
    }

    @Test
    void keepsAnExplicitHttpsScheme() {
        assertThat(validator.normalize("https://8.8.8.8")).isEqualTo("https://8.8.8.8");
    }

    @Test
    void lowercasesTheHost() {
        assertThat(validator.normalize("https://[2001:DB8::AB]")).isEqualTo("https://[2001:db8::ab]");
    }

    @Test
    void rejectsHttp() {
        assertThatThrownBy(() -> validator.normalize("http://canvas.yourschool.edu"))
                .isInstanceOf(RequestValidationException.class)
                .hasMessageContaining("https");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ftp://canvas.yourschool.edu",
            "https://user:secret@canvas.yourschool.edu",
            "https://canvas.yourschool.edu:8080",
            "https://canvas.yourschool.edu/api/v1/users",
            "https://canvas.yourschool.edu?redirect=elsewhere",
            "https://canvas.yourschool.edu#fragment",
            "https://",
            "not a url"
    })
    void rejectsMalformedOrUnsafeUrls(String input) {
        assertThatThrownBy(() -> validator.normalize(input))
                .isInstanceOf(RequestValidationException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsBlankInput(String input) {
        assertThatThrownBy(() -> validator.normalize(input))
                .isInstanceOf(RequestValidationException.class);
    }

    @Test
    void rejectsNullInput() {
        assertThatThrownBy(() -> validator.normalize(null))
                .isInstanceOf(RequestValidationException.class);
    }

    @Test
    void allowsPort443Explicitly() {
        assertThat(validator.normalize("https://8.8.8.8:443")).isEqualTo("https://8.8.8.8");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://127.0.0.1",
            "https://10.0.0.1",
            "https://192.168.1.1",
            "https://172.16.0.1",
            "https://169.254.1.1",
            "https://100.64.0.1",
            "https://0.0.0.0"
    })
    void rejectsPrivateAndInternalAddresses(String input) {
        assertThatThrownBy(() -> validator.normalize(input))
                .isInstanceOf(RequestValidationException.class)
                .hasMessageContaining("private network");
    }

    @Test
    void allowsAPublicAddressWhenNoSuffixListIsSet() {
        assertThat(validator.normalize("https://8.8.8.8")).isEqualTo("https://8.8.8.8");
    }

    @Test
    void allowsAHostThatMatchesTheSuffixList() {
        allowSuffixes(List.of("8.8.8.8"));
        assertThat(validator.normalize("https://8.8.8.8")).isEqualTo("https://8.8.8.8");
    }

    @Test
    void rejectsAHostOutsideTheSuffixList() {
        allowSuffixes(List.of("instructure.com"));
        assertThatThrownBy(() -> validator.normalize("https://8.8.8.8"))
                .isInstanceOf(RequestValidationException.class)
                .hasMessageContaining("isn't allowed");
    }
}
