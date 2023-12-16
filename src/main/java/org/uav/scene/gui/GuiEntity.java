package org.uav.scene.gui;

import org.uav.UavVisualization;
import org.uav.config.Config;
import org.uav.config.DroneParameters;
import org.uav.messages.MessageBoard;
import org.uav.model.SimulationState;
import org.uav.scene.gui.widget.ControlPanelWidget;
import org.uav.scene.gui.widget.artificialHorizon.ArtificialHorizonWidget;
import org.uav.scene.gui.widget.debug.DebugWidget;
import org.uav.scene.gui.widget.map.MapWidget;
import org.uav.scene.gui.widget.messageBoard.MessageBoardWidget;
import org.uav.scene.gui.widget.projectiles.ProjectileWidget;
import org.uav.scene.gui.widget.propellersDisplay.PropellersDisplayWidget;
import org.uav.scene.gui.widget.radar.RadarWidget;
import org.uav.scene.shader.Shader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;

import static org.lwjgl.opengl.GL11C.GL_ALWAYS;
import static org.uav.utils.OpenGLUtils.drawWithDepthFunc;

public class GuiEntity {
    private Shader guiShader;
    private boolean drawGui;
    private final ControlPanelWidget controlPanel;
    private final RadarWidget radar;
    private final ArtificialHorizonWidget artificialHorizon;
    private final MapWidget map;
    private final PropellersDisplayWidget rotorDisplay;
    private final ProjectileWidget projectiles;
    private final MessageBoardWidget messageBoardWidget;
    private final DebugWidget debug;

    public GuiEntity(SimulationState simulationState, Config config, DroneParameters droneParameters, MessageBoard messageBoard) throws IOException {
        setUpShader(config.getGraphicsSettings().getUseGammaCorrection(), config.getGraphicsSettings().getGammaCorrection());
        drawGui = config.getGraphicsSettings().getEnableGui();
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

        projectiles = new ProjectileWidget(background, simulationState, config);

        messageBoardWidget = new MessageBoardWidget(config, messageBoard);

        debug = new DebugWidget(background, simulationState, config);
    }

    private void setUpShader(boolean useGammaCorrection, float gammaCorrection) throws IOException {
        var guiVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/gui/guiShader.vert"));
        var guiFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/gui/guiShader.frag"));
        guiShader = new Shader(guiVertexShaderSource, guiFragmentShaderSource);
        guiShader.use();
        guiShader.setBool("useGammaCorrection", useGammaCorrection);
        guiShader.setFloat("gammaCorrection", gammaCorrection);
    }

    public static BufferedImage loadImage(String path) {
        try {
            return  ImageIO.read(new File(path));
        } catch (IOException e) {

            throw new RuntimeException("Failed to load " + path);
        }
    }

    public void draw(boolean isMapOverlay) {
        openMap(isMapOverlay);
        update();
        drawWithDepthFunc(this::draw, GL_ALWAYS);
    }

    private void draw() {
        debug.draw(guiShader);
        if(!drawGui) return;
        controlPanel.draw(guiShader);
        artificialHorizon.draw(guiShader);
        radar.draw(guiShader);
        rotorDisplay.draw(guiShader);
        map.draw(guiShader);
        projectiles.draw(guiShader);
        messageBoardWidget.draw(guiShader);
    }

    public void update() {
        debug.update();
        if(!drawGui) return;
        radar.update();
        artificialHorizon.update();
        rotorDisplay.update();
        map.update();
        projectiles.update();
    }

    public void openMap(boolean open) {
        map.setHidden(!open);
    }
}
