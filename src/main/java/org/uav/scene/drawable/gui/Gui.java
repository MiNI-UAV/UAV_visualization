package org.uav.scene.drawable.gui;

import org.uav.config.Config;
import org.uav.model.SimulationState;
import org.uav.scene.drawable.gui.widget.artificialHorizon.ArtificialHorizonWidget;
import org.uav.scene.drawable.gui.widget.map.MapWidget;
import org.uav.scene.drawable.gui.widget.radar.RadarWidget;
import org.uav.scene.shader.Shader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Gui {
    private final RadarWidget radar;
    private final ArtificialHorizonWidget artificialHorizon;
    private final MapWidget map;

    public Gui(SimulationState simulationState, Config config) {
        var assetsDirectory = simulationState.getAssetsDirectory() + "/core/GUI/";
        radar = new RadarWidget(
                loadImage(assetsDirectory + "radar.png"),
                loadImage(assetsDirectory + "radarArrow.png"),
                simulationState,
                config
        );
        artificialHorizon = new ArtificialHorizonWidget(
                loadImage(assetsDirectory + "horizon.png"),
                loadImage(assetsDirectory + "horizonCursor.png"),
                loadImage(assetsDirectory + "horizonRoll.png"),
                loadImage(assetsDirectory + "compass.png"),
                simulationState,
                config
        );
        String mapPath = simulationState.getAssetsDirectory() + "/maps/" + simulationState.getServerMap() + "/model/minimap.png";
        map = new MapWidget(
                loadImage(assetsDirectory + "background.png"),
                loadImage(mapPath),
                loadImage(assetsDirectory + "droneIconLowRes.png"),
                simulationState,
                config
        );
    }

    public static BufferedImage loadImage(String path) {
        try {
            return  ImageIO.read(new File(path));
        } catch (IOException e) {

            throw new RuntimeException();
        }
    }

    public void draw(Shader shader) {
        artificialHorizon.draw(shader);
        radar.draw(shader);
        map.draw(shader);
    }

    public void update() {
        radar.update();
        artificialHorizon.update();
        map.update();
    }

    public void openMap(boolean open) {
        map.setHidden(!open);
    }
}
