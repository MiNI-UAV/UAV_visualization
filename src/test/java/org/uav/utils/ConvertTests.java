package org.uav.utils;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.uav.utils.Convert.toEuler;
import static org.uav.utils.Convert.toQuaternion;

public class ConvertTests {

    @ParameterizedTest
    @MethodSource("toQuaternionTestSource")
    public void toQuaternionTest(Vector3f actual, Quaternionf expected, float delta) {
        Quaternionf actualQ = toQuaternion(actual);
        assertEquals(actualQ.x, expected.x, delta);
        assertEquals(actualQ.y, expected.y, delta);
        assertEquals(actualQ.z, expected.z, delta);
        assertEquals(actualQ.w, expected.w, delta);
    }
    static Stream<Arguments> toQuaternionTestSource() {
        return Stream.of(
                Arguments.of(new Vector3f(0, 0, 0), new Quaternionf(0, 0, 0, 1), 0),
                Arguments.of(new Vector3f(3.142f, 1.571f, 3.142f), new Quaternionf(0.0, 0.707, 0.0, 0.707), 0.001f)
        );
    }
    @ParameterizedTest
    @MethodSource("toEulerTestSource")
    public void toEulerTest(Quaternionf actual, Vector3f expected, float delta) {
        Vector3f actualE = toEuler(actual);
        assertEquals(actualE.x, expected.x, delta);
        assertEquals(actualE.y, expected.y, delta);
        assertEquals(actualE.z, expected.z, delta);
    }
    static Stream<Arguments> toEulerTestSource() {
        return Stream.of(
                Arguments.of(new Quaternionf(0, 0, 0, 1), new Vector3f(0, 0, 0), 0)
        );
    }
}
