package org.uav.presentation.entity.gui.widget.propellersDisplay;

import org.joml.*;
import org.lwjgl.system.MemoryStack;
import org.uav.logic.config.Config;
import org.uav.logic.config.DroneParameters;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.text.TextEngine;
import org.uav.presentation.entity.vector.VectorCircle;
import org.uav.presentation.entity.vector.VectorCircleArc;
import org.uav.presentation.rendering.Shader;

import java.awt.*;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.uav.utils.OpenGLUtils.OPENGL_CANVAS_SIZE;

public class PropellersDisplayLayer {
    private final Supplier<RuntimeException> noRotors = () -> new RuntimeException("No rotors on drone!");
    private final Vector2i canvasSize;
    private final Vector2i noMarginCanvasSize;
    private final List<Vector3f> rotors;
    private final List<Integer> rotationDirection;
    private List<Vector2i> scaledPropellers;
    private int propellerRadius;
    private final Vector2i displayMargin;
    private final Vector2i propellerPanelPadding;
    private final int textHeight;
    private final float textHeightNorm;
    private final int minimalTextWidth;
    private final List<Float> maxRPMs;
    private List<Float> propellerRPMs;

    // Shapes
    private static final int CIRCLE_CORNER_COUNT = 30;
    private final VectorCircle propellerBackgroundShape;
    private final VectorCircle propellerCenterShape;
    private final VectorCircleArc propellerArcShape;
    private final TextEngine textEngine;

    public PropellersDisplayLayer(Vector2i canvasSize, Vector4f widgetPosition, DroneParameters droneParameters, Shader vectorShader, Shader circleArcShader, Shader textShader, Config config) {
        this.canvasSize = canvasSize;
        textHeight = 30;
        textHeightNorm = (float) textHeight / 1080;
        textEngine = new TextEngine(widgetPosition, textHeightNorm, textShader, config);
        displayMargin = new Vector2i((int) (0.025 * canvasSize.x), (int) (0.025 * canvasSize.y));
        noMarginCanvasSize = new Vector2i(canvasSize.x - 2*displayMargin.x, canvasSize.y - 2*displayMargin.y);
        propellerPanelPadding = new Vector2i((int) (0.0125 * canvasSize.x), (int) (0.0125 * canvasSize.y));
        minimalTextWidth = 0;
        if(droneParameters.getRotors() != null && droneParameters.getRotors().getRotor() != null && !droneParameters.getRotors().getRotor().isEmpty()) {
            rotors = droneParameters.getRotors().getRotor().stream()
                    .map(r -> new Vector3f(r.getPosition().x, r.getPosition().y, r.getPosition().z)).toList();
            rotationDirection = droneParameters.getRotors().getRotor().stream()
                    .map(DroneParameters.Rotors.Rotor::getDirection).toList();
            maxRPMs = droneParameters.getRotors().getRotor().stream()
                    .map(rotor -> rotor.getMaxSpeed() / (2.0f*(float)Math.PI) * 60.0f)
                    .toList();
            propellerRPMs = new ArrayList<>();
            initRadius();
            propellerBackgroundShape = new VectorCircle(new Vector2f(), (float) propellerRadius / canvasSize.x * OPENGL_CANVAS_SIZE, CIRCLE_CORNER_COUNT, vectorShader);
            propellerCenterShape = new VectorCircle(new Vector2f(), 3f / canvasSize.x * OPENGL_CANVAS_SIZE, 6, vectorShader);
            propellerArcShape = new VectorCircleArc(new Vector2f(), (float) propellerRadius / canvasSize.x * OPENGL_CANVAS_SIZE, CIRCLE_CORNER_COUNT, vectorShader, circleArcShader);
        }
        else {
            rotors = new ArrayList<>();
            rotationDirection = new ArrayList<>();
            maxRPMs = new ArrayList<>();
            propellerRPMs = new ArrayList<>();
            scaledPropellers = new ArrayList<>();
            propellerBackgroundShape = null;
            propellerCenterShape = null;
            propellerArcShape = null;
        }

    }

    private void initRadius() {
        int leftBound = 0;
        int rightBound = (Math.min(noMarginCanvasSize.x, noMarginCanvasSize.y) + 1) / 2;
        int radius = (rightBound + leftBound) / 2;

        while(leftBound < rightBound && leftBound != radius) {
            scaledPropellers = getScaledRotorsForRadius(radius);
            boolean rotorsFitted = checkIfRotorsFit(scaledPropellers, radius);
            if(rotorsFitted) {
                leftBound = radius;
            } else {
                rightBound = radius;
            }
            radius = (rightBound + leftBound) / 2;
        }
        if(radius == 0)
            throw new RuntimeException("Rotor display with current parameters couldn't find fitting radius");
        propellerRadius = radius;
    }

    private List<Vector2i> getScaledRotorsForRadius(int radius) {
        Vector2i noPanelCanvasSize = new Vector2i(
                noMarginCanvasSize.x - Math.max(2 * radius, minimalTextWidth) - 2 * propellerPanelPadding.x,
                noMarginCanvasSize.y - Math.max(2 * radius, minimalTextWidth) - 2 * propellerPanelPadding.y - textHeight
        );
        float xMax = rotors.stream().max((Vector3f v1, Vector3f v2) -> Float.compare(v1.x,v2.x)).orElseThrow(noRotors).x;
        float yMax = rotors.stream().max((Vector3f v1, Vector3f v2) -> Float.compare(v1.y,v2.y)).orElseThrow(noRotors).y;
        float xMin = rotors.stream().min((Vector3f v1, Vector3f v2) -> Float.compare(v1.x,v2.x)).orElseThrow(noRotors).x;
        float yMin = rotors.stream().min((Vector3f v1, Vector3f v2) -> Float.compare(v1.y,v2.y)).orElseThrow(noRotors).y;
        float xDist = 2 * Math.max(Math.abs(xMax), Math.abs(xMin));
        float yDist = 2 * Math.max(Math.abs(yMax), Math.abs(yMin));
        if(xDist == 0 && yDist == 0) return rotors.stream().map(v -> new Vector2i(0)).toList();
        float xScale = xDist == 0? noPanelCanvasSize.y / yDist: noPanelCanvasSize.x / xDist;
        float yScale = yDist == 0? noPanelCanvasSize.x / xDist: noPanelCanvasSize.y / yDist;
        Vector2f scale = new Vector2f(xScale, yScale);
        float fitScale = Math.min(scale.x, scale.y);
        return rotors.stream().map(v -> new Vector2i((int) (v.x * fitScale), (int) (v.y * fitScale))).toList();
    }

    private boolean checkIfRotorsFit(List<Vector2i> scaledRotors, int radius) {
        for(int i=0; i<scaledRotors.size() - 1; i++) {
            Vector2i a = scaledRotors.get(i);
            for(int j=i+1; j<scaledRotors.size(); j++) {
                Vector2i b = scaledRotors.get(j);
                int panelWidth = Math.max(2 * radius, minimalTextWidth) + 2 * propellerPanelPadding.x;
                int panelHeight = 2 * radius + textHeight + 2 * propellerPanelPadding.y;
                Rectangle aRect = new Rectangle(
                        a.x - Math.max(radius, minimalTextWidth/2) - propellerPanelPadding.x,
                        a.y - radius - propellerPanelPadding.y,
                        panelWidth, panelHeight
                );
                Rectangle bRect = new Rectangle(
                        b.x - Math.max(radius, minimalTextWidth/2) - propellerPanelPadding.x,
                        b.y - radius - propellerPanelPadding.y,
                        panelWidth, panelHeight
                );
                if(intersectRect(aRect, bRect)) return false;
            }
        }
        return true;
    }

    // Adapted from https://stackoverflow.com/questions/2752349/fast-rectangle-to-rectangle-intersection
    private boolean intersectRect(Rectangle r1, Rectangle r2) {
        return !(r2.x > r1.x + r1.width ||
                r2.x + r2.width < r1.x ||
                r2.y > r1.y + r1.height ||
                r2.y + r2.height < r1.y);
    }

    public void update(SimulationState simulationState) {
        simulationState.getPlayerDrone().ifPresent(drone ->
                propellerRPMs = drone.droneStatus.propellersRadps.stream().map(radps -> radps / (2 * (float) Math.PI) * 60).toList()
        );
    }

    public void draw(MemoryStack stack) {
        if (scaledPropellers.size() > propellerRPMs.size()) return;
        for (int i = 0; i < scaledPropellers.size(); i++) {
            var rotor = scaledPropellers.get(i);
            float rotorXNorm = (float) rotor.x / canvasSize.x * OPENGL_CANVAS_SIZE;
            float rotorYNorm = (float) (rotor.y + textHeight/2) / canvasSize.y * OPENGL_CANVAS_SIZE; // - textHeight / 2
            float radiusNorm = (float) propellerRadius / canvasSize.x * OPENGL_CANVAS_SIZE;

            var transform = new Matrix3x2f();
            transform.translate(rotorXNorm, rotorYNorm);
            propellerBackgroundShape.setTransform(transform);
            propellerBackgroundShape.setColor(new Color(0, 0, 0, 0.4f));
            propellerBackgroundShape.draw(stack);

            propellerCenterShape.setTransform(transform);
            propellerCenterShape.draw(stack);

            int rpms = propellerRPMs.get(i).intValue();
            float ratio = (float) rpms / maxRPMs.get(i);
            propellerArcShape.setStartAngle((float)Math.PI / 2 * 3);
            propellerArcShape.setArcAngle(ratio * 2 * (float)Math.PI * -rotationDirection.get(i));
            propellerArcShape.setTransform(transform);
            propellerArcShape.draw(stack);

            String rpmsString = rpms + " rpm";
            textEngine.setPosition(rotorXNorm - textEngine.getStringWidth(rpmsString) / 2, - (rotorYNorm + radiusNorm - textHeightNorm));
            textEngine.renderText(rpmsString);
        }
    }
}
