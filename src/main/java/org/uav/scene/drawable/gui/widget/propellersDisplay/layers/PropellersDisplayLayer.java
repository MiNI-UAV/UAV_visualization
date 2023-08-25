package org.uav.scene.drawable.gui.widget.propellersDisplay.layers;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.uav.config.DroneParameters;
import org.uav.model.SimulationState;
import org.uav.scene.drawable.gui.DrawableGuiLayer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PropellersDisplayLayer implements DrawableGuiLayer {
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
    private final int minimalTextWidth;
    private final float maxRPMs;
    private List<Float> propellerRPMs;
    private int propellerUpdateFrequency = 2; // TODO [MU-78] Tie update frequency to fps
    private int propellerUpdateTick = 0;



    public PropellersDisplayLayer(Vector2i canvasSize, DroneParameters droneParameters) {
        this.canvasSize = canvasSize;
        displayMargin = new Vector2i((int) (0.025 * canvasSize.x), (int) (0.025 * canvasSize.y));
        noMarginCanvasSize = new Vector2i(canvasSize.x - 2*displayMargin.x, canvasSize.y - 2*displayMargin.y);
        propellerPanelPadding = new Vector2i((int) (0.0125 * canvasSize.x), (int) (0.0125 * canvasSize.y));
        textHeight = 20;
        minimalTextWidth = 0;
        rotors = droneParameters.getRotors().getPositions();
        rotationDirection = droneParameters.getRotors().getDirection();
        maxRPMs = droneParameters.getControl().getMaxSpeed() / (2*(float)Math.PI) * 60;
        propellerRPMs = new ArrayList<>();
        initRadius();
    }

    private void initRadius() {
        int leftBound = 0;
        int rightBound = Math.min(noMarginCanvasSize.x, noMarginCanvasSize.y) + 1;
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
        Vector2f scale = new Vector2f(noPanelCanvasSize.x / xDist, noPanelCanvasSize.y / yDist);
        float fitScale = Math.min(scale.x, scale.y);
        return rotors.stream().map(v -> new Vector2i((int) (v.x * fitScale), (int) (v.y * fitScale))).toList();
    }

    private boolean checkIfRotorsFit(List<Vector2i> scaledRotors, int radius) {
        for(int i=0; i<scaledRotors.size() - 1; i++) {
            for(int j=i+1; j<scaledRotors.size(); j++) {
                Vector2i a = scaledRotors.get(i);
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
        var drone = simulationState.getCurrPassDroneStatuses().map.get(simulationState.getCurrentlyControlledDrone().getId());
        if(drone == null) return;
        if(propellerUpdateTick > propellerUpdateFrequency) {
            propellerUpdateTick = 0;
            propellerRPMs = drone.propellersRadps.stream().map(radps -> radps / (2*(float)Math.PI) * 60).toList();
        }
        propellerUpdateTick++;
    }

    @Override
    public void draw(Graphics2D g) {
        if (scaledPropellers.size() > propellerRPMs.size()) return;
        for (int i = 0; i < scaledPropellers.size(); i++) {
            var rotor = scaledPropellers.get(i);
            g.setColor(new Color(0, 0, 0, 0.4f));
            g.fillOval(
                    rotor.x - propellerRadius + canvasSize.x / 2,
                    rotor.y - propellerRadius + canvasSize.y / 2 - textHeight / 2,
                    2 * propellerRadius,
                    2 * propellerRadius
            );
            g.setColor(Color.WHITE);
            g.fillOval(rotor.x - 3 + canvasSize.x / 2, rotor.y - 3 + canvasSize.y / 2 - textHeight / 2, 6, 6);

            int rpms = propellerRPMs.get(i).intValue();
            float ratio = (float) rpms / maxRPMs;
            g.fillArc(
                    rotor.x - propellerRadius + canvasSize.x / 2,
                    rotor.y - propellerRadius + canvasSize.y / 2 - textHeight / 2,
                    2 * propellerRadius,
                    2 * propellerRadius,
                    90,
                    (int) (360 * ratio) * -rotationDirection.get(i));

            String rpmsString = rpms + " rpm";
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(new Font("SansSerif", Font.PLAIN, textHeight));
            g.drawString(
                    rpmsString,
                    canvasSize.x / 2 + rotor.x - g.getFontMetrics().stringWidth(rpmsString) / 2,
                    canvasSize.y / 2 + rotor.y + propellerRadius + textHeight / 2);
        }
    }
}
