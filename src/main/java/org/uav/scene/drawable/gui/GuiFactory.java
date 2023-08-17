package org.uav.scene.drawable.gui;

import org.uav.UavVisualization;
import org.uav.config.Config;
import org.uav.model.SimulationState;
import org.uav.scene.drawable.gui.widget.artificialHorizon.ArtificialHorizonWidget;
import org.uav.scene.drawable.gui.widget.map.MapWidget;
import org.uav.scene.drawable.gui.widget.radar.RadarWidget;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class GuiFactory {

    static public Gui createStandardGui(SimulationState simulationState, Config config) {
        var guiBuilder = new Gui.GuiBuilder();
        var assetsDirectory = "assets/textures/GUI/";
        var radar = new RadarWidget(
                loadImage(assetsDirectory + "radar.png"),
                loadImage(assetsDirectory + "radarArrow.png"),
                simulationState,
                config
        );
        var artificialHorizon = new ArtificialHorizonWidget(
                loadImage(assetsDirectory + "horizon3.png"),
                loadImage(assetsDirectory + "horizonCursor.png"),
                loadImage(assetsDirectory + "horizonRoll.png"),
                loadImage(assetsDirectory + "compass.png"),
                simulationState,
                config
        );
        String mapPath = assetsDirectory + "maps/" + config.map + ".png";
        var map = new MapWidget(
                loadImage(assetsDirectory + "background.png"),
                loadImage(mapPath),
                loadImage(assetsDirectory + "droneIconLowRes.png"),
                simulationState,
                config
        );

        return guiBuilder
                .addGuiElement("radar", radar)
                .addGuiElement("artificialHorizon", artificialHorizon)
                .addGuiElement("map", map)
                .build();
    }

    public static BufferedImage loadImage(String path) {
        try {
            return  ImageIO.read(new File(Objects.requireNonNull(UavVisualization.class.getClassLoader().getResource(path)).getPath()));
        } catch (IOException e) {

            throw new RuntimeException();
        }
    }
}
