package org.uav.scene.drawable.gui.widget.rotorDisplay.layers;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.uav.model.SimulationState;
import org.uav.scene.drawable.gui.DrawableGuiLayer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RotorDisplayLayer implements DrawableGuiLayer {
    private final Supplier<RuntimeException> noRotors = () -> new RuntimeException("No rotors on drone!");

    Vector2i canvasSize;
    Vector2i noMarginCanvasSize;
    List<Vector3f> rotors;
    List<Boolean> clockwiseRotation;
    List<Vector2i> scaledRotors;
    int rotorRadius;
    Vector2i displayMargin;
    Vector2i rotorPadding;
    int textHeight;
    int minimalTextWidth;
    List<Float> rotorRPMs;
    int maxRPMS = 1000;



    public RotorDisplayLayer(Vector2i canvasSize) {
        this.canvasSize = canvasSize;
        displayMargin = new Vector2i((int) (0.025 * canvasSize.x), (int) (0.025 * canvasSize.y));
        noMarginCanvasSize = new Vector2i(canvasSize.x - 2*displayMargin.x, canvasSize.y - 2*displayMargin.y);
        rotorPadding = new Vector2i((int) (0.0125 * canvasSize.x), (int) (0.0125 * canvasSize.y));
        textHeight = 20;
        minimalTextWidth = 0;//93;
        rotors = new ArrayList<>();
        clockwiseRotation = new ArrayList<>();
        initRadius();
    }

    private void initRadius() {
        // TODO Add drone config as a parameter
        rotors = List.of(
                new Vector3f(0.25f, 0.25f, 0),
                new Vector3f(-0.25f, 0.25f, 0),
                new Vector3f(-0.25f, -0.25f, 0),
                new Vector3f(0.25f, -0.25f, 0)
        );
        clockwiseRotation = List.of(true, false, true, false);

        int leftBound = 0;
        int rightBound = Math.min(noMarginCanvasSize.x, noMarginCanvasSize.y) + 1;
        int radius = (rightBound + leftBound) / 2;

        while(leftBound < rightBound && leftBound != radius) {
            scaledRotors = getScaledRotorsForRadius(radius);
            boolean rotorsFitted = checkIfRotorsFit(scaledRotors, radius);
            if(rotorsFitted) {
                leftBound = radius;
            } else {
                rightBound = radius;
            }
            radius = (rightBound + leftBound) / 2;
        }
        if(radius == 0)
            throw new RuntimeException("Rotor display with current parameters couldn't find fitting radius");
        rotorRadius = radius;
    }

    private List<Vector2i> getScaledRotorsForRadius(int radius) {
        Vector2i noPanelCanvasSize = new Vector2i(
                noMarginCanvasSize.x - Math.max(2 * radius, minimalTextWidth) - 2 * rotorPadding.x,
                noMarginCanvasSize.y - Math.max(2 * radius, minimalTextWidth) - 2 * rotorPadding.y - textHeight
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
                int panelWidth = Math.max(2 * radius, minimalTextWidth) + 2 * rotorPadding.x;
                int panelHeight = 2 * radius + textHeight + 2 * rotorPadding.y;
                Rectangle aRect = new Rectangle(
                        a.x - Math.max(radius, minimalTextWidth/2) - rotorPadding.x,
                        a.y - radius - rotorPadding.y,
                        panelWidth, panelHeight
                );
                Rectangle bRect = new Rectangle(
                        b.x - Math.max(radius, minimalTextWidth/2) - rotorPadding.x,
                        b.y - radius - rotorPadding.y,
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
        rotorRPMs = new ArrayList<>(drone.propellers);
    }

    @Override
    public void draw(Graphics2D g) {
        if (scaledRotors.size() > rotorRPMs.size()) return;
        for (int i = 0; i < scaledRotors.size(); i++) {
            var rotor = scaledRotors.get(i);
            g.setColor(new Color(0, 0, 0, 0.4f));
            g.fillOval(
                    rotor.x - rotorRadius + canvasSize.x / 2,
                    rotor.y - rotorRadius + canvasSize.y / 2 - textHeight / 2,
                    2 * rotorRadius,
                    2 * rotorRadius
            );
            g.setColor(Color.WHITE);
            g.fillOval(rotor.x - 3 + canvasSize.x / 2, rotor.y - 3 + canvasSize.y / 2 - textHeight / 2, 6, 6);

            int rpms = rotorRPMs.get(i).intValue(); // TODO Significant nums based on max
            float ratio = (float) rpms / maxRPMS;
            g.fillArc(
                    rotor.x - rotorRadius + canvasSize.x / 2,
                    rotor.y - rotorRadius + canvasSize.y / 2 - textHeight / 2,
                    2 * rotorRadius,
                    2 * rotorRadius,
                    90,
                    (int) (360 * ratio) * (clockwiseRotation.get(i) ? -1 : 1));

            String rpmsString = rpms + " rpm"; // TODO covert to rpms
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(new Font("SansSerif", Font.PLAIN, textHeight));
            g.drawString(
                    rpmsString,
                    canvasSize.x / 2 + rotor.x - g.getFontMetrics().stringWidth(rpmsString) / 2,
                    canvasSize.y / 2 + rotor.y + rotorRadius + textHeight / 2);
        }
    }
}
