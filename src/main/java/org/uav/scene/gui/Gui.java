package org.uav.scene.gui;

import org.uav.config.Config;
import org.uav.config.DroneParameters;
import org.uav.model.SimulationState;
import org.uav.scene.gui.widget.ControlPanelWidget;
import org.uav.scene.gui.widget.artificialHorizon.ArtificialHorizonWidget;
import org.uav.scene.gui.widget.map.MapWidget;
import org.uav.scene.gui.widget.propellersDisplay.PropellersDisplayWidget;
import org.uav.scene.gui.widget.radar.RadarWidget;
import org.uav.scene.shader.Shader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class Gui {
    private final ControlPanelWidget controlPanel;
    private final RadarWidget radar;
    private final ArtificialHorizonWidget artificialHorizon;
    private final MapWidget map;
    private final PropellersDisplayWidget rotorDisplay;

    public Gui(SimulationState simulationState, Config config, DroneParameters droneParameters) {
        var assetsDirectory = Paths.get(simulationState.getAssetsDirectory() , "core", "GUI");
        var background = loadImage(Paths.get(assetsDirectory.toString(), "background.png").toString());

        controlPanel = new ControlPanelWidget(background, config);

        radar = new RadarWidget(
                loadImage(Paths.get(assetsDirectory.toString(), "radar.png").toString()),
                loadImage(Paths.get(assetsDirectory.toString(), "radarArrow.png").toString()),
                simulationState,
                config
        );

        artificialHorizon = new ArtificialHorizonWidget(
                loadImage(Paths.get(assetsDirectory.toString(), "horizon.png").toString()),
                loadImage(Paths.get(assetsDirectory.toString(), "horizonCursor.png").toString()),
                loadImage(Paths.get(assetsDirectory.toString(), "horizonRoll.png").toString()),
                loadImage(Paths.get(assetsDirectory.toString(), "compass.png").toString()),
                simulationState,
                config
        );

        rotorDisplay = new PropellersDisplayWidget(simulationState, config, droneParameters);

        String mapPath = Paths.get(simulationState.getAssetsDirectory(), "maps", simulationState.getServerMap(), "model", "minimap.png").toString();
        map = new MapWidget(
                background,
                loadImage(mapPath),
                loadImage(Paths.get(assetsDirectory.toString(), "droneIconLowRes.png").toString()),
                loadImage(Paths.get(assetsDirectory.toString(), "droneIconLowResDemanded.png").toString()),
                simulationState,
                config
        );
    }

    public static BufferedImage loadImage(String path) {
        try {
            return  ImageIO.read(new File(path));
        } catch (IOException e) {

            throw new RuntimeException("Failed to load " + path);
        }
    }

    public void draw(Shader shader) {
        controlPanel.draw(shader);
        artificialHorizon.draw(shader);
        radar.draw(shader);
        rotorDisplay.draw(shader);
        map.draw(shader);
    }

    public void update() {
        radar.update();
        artificialHorizon.update();
        rotorDisplay.update();
        map.update();
    }

    public void openMap(boolean open) {
        map.setHidden(!open);
    }
}
