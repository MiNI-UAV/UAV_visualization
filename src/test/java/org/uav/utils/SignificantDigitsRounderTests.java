package org.uav.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.uav.utils.SignificantDigitsRounder.sigDigRounder;

public class SignificantDigitsRounderTests {
    @ParameterizedTest
    @MethodSource("SignificantDigitsRounderTestSource")
    public void SignificantDigitsRounderTest(double number, int digits, int round, double expected, double delta) {
        assertEquals(sigDigRounder(number, digits, round), expected, delta);
    }
    static Stream<Arguments> SignificantDigitsRounderTestSource() {
        return Stream.of(
                Arguments.of(1.1256, 3, -1, 1.12, 0),
                Arguments.of(1.1256, 3, 0, 1.13, 0.0000001),
                Arguments.of(1.1256, 3, 1, 1.13, 0.0000001),
                Arguments.of(1.1256, 1, 1, 2, 0),
                Arguments.of(1.1256, 0, 0, 0, 0)
        );
    }
}
