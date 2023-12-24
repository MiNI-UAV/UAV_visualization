package org.uav.presentation.entity.gui;

import org.uav.UavVisualization;
import org.uav.logic.config.Config;
import org.uav.logic.config.DroneParameters;
import org.uav.logic.messages.MessageBoard;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.gui.widget.ControlPanelWidget;
import org.uav.presentation.entity.gui.widget.artificialHorizon.ArtificialHorizonWidget;
import org.uav.presentation.entity.gui.widget.debug.DebugWidget;
import org.uav.presentation.entity.gui.widget.map.MapWidget;
import org.uav.presentation.entity.gui.widget.messageBoard.CriticalMessageBoardWidget;
import org.uav.presentation.entity.gui.widget.messageBoard.MessageBoardWidget;
import org.uav.presentation.entity.gui.widget.projectiles.ProjectileWidget;
import org.uav.presentation.entity.gui.widget.propellersDisplay.PropellersDisplayWidget;
import org.uav.presentation.entity.gui.widget.radar.RadarWidget;
import org.uav.presentation.rendering.Shader;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;

import static org.lwjgl.opengl.GL11C.GL_ALWAYS;
import static org.uav.utils.IOUtils.loadImage;
import static org.uav.utils.OpenGLUtils.drawWithDepthFunc;

public class GuiEntity {
    private Shader spriteShader;
    private Shader vectorShader;
    private Shader circleArcShader;
    private Shader textShader;
    private boolean drawGui;
    private final ControlPanelWidget controlPanel;
    private final RadarWidget radar;
    private final ArtificialHorizonWidget artificialHorizon;
    private final MapWidget map;
    private final PropellersDisplayWidget propellersDisplay;
    private final ProjectileWidget projectiles;
    private final MessageBoardWidget messageBoardWidget;
    private final CriticalMessageBoardWidget criticalMessageBoardWidget;
    private final DebugWidget debug;

    public GuiEntity(SimulationState simulationState, Config config, DroneParameters droneParameters, MessageBoard messageBoard) throws IOException {
        setUpShaders(config.getGraphicsSettings().getGammaCorrection());
        drawGui = config.getGraphicsSettings().getEnableGui();
        var assetsDirectory = Paths.get(simulationState.getAssetsDirectory() , "core", "GUI");
        var background = loadImage(Paths.get(assetsDirectory.toString(), "background.png").toString());

        controlPanel = new ControlPanelWidget(background, spriteShader, config);

        radar = new RadarWidget(
                loadImage(Paths.get(assetsDirectory.toString(), "radar.png").toString()),
                loadImage(Paths.get(assetsDirectory.toString(), "radarArrow.png").toString()),
                spriteShader,
                vectorShader,
                config
        );
        artificialHorizon = new ArtificialHorizonWidget(assetsDirectory, spriteShader, vectorShader, textShader, config);

        propellersDisplay = new PropellersDisplayWidget(simulationState, droneParameters, vectorShader, circleArcShader, textShader, config);

        String mapPath = Paths.get(simulationState.getAssetsDirectory(), "maps", simulationState.getServerMap(), "model", "minimap.png").toString();
        map = new MapWidget(
                background,
                loadImage(mapPath),
                loadImage(Paths.get(assetsDirectory.toString(), "droneIconLowRes.png").toString()),
                loadImage(Paths.get(assetsDirectory.toString(), "droneIconLowResDemanded.png").toString()),
                simulationState,
                spriteShader,
                vectorShader,
                config
        );

        projectiles = new ProjectileWidget(background, spriteShader, vectorShader, circleArcShader, textShader, config);

        messageBoardWidget = new MessageBoardWidget(textShader, config, messageBoard);
        criticalMessageBoardWidget = new CriticalMessageBoardWidget(textShader, config, messageBoard);

        debug = new DebugWidget(background, simulationState, spriteShader, textShader, config);
    }

    private void setUpShaders(float gammaCorrection) throws IOException {
        var spriteVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/sprite/spriteShader.vert"));
        var spriteFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/sprite/spriteShader.frag"));
        spriteShader = new Shader(spriteVertexShaderSource, spriteFragmentShaderSource);
        spriteShader.use();
        spriteShader.setFloat("gammaCorrection", gammaCorrection);

        var vectorVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/vector/vectorShader.vert"));
        var vectorFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/vector/vectorShader.frag"));
        vectorShader = new Shader(vectorVertexShaderSource, vectorFragmentShaderSource);
        vectorShader.use();
        vectorShader.setFloat("gammaCorrection", gammaCorrection);

        var circleArcVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/vector/circleArc/circleArcShader.vert"));
        var circleArcFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/vector/circleArc/circleArcShader.frag"));
        circleArcShader = new Shader(circleArcVertexShaderSource, circleArcFragmentShaderSource);
        circleArcShader.use();
        circleArcShader.setFloat("gammaCorrection", gammaCorrection);

        var textVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/text/textShader.vert"));
        var textFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/text/textShader.frag"));
        textShader = new Shader(textVertexShaderSource, textFragmentShaderSource);
        textShader.use();
        textShader.setFloat("gammaCorrection", gammaCorrection);
    }

    public void draw(SimulationState simulationState) {
        openMap(simulationState.isMapOverlay());
        update(simulationState);
        drawWithDepthFunc(this::draw, GL_ALWAYS);
    }

    private void draw() {
        debug.draw();
        if(!drawGui) return;
        controlPanel.draw();
        artificialHorizon.draw();
        radar.draw();
        propellersDisplay.draw();
        if(!map.isHidden()) map.draw();
        projectiles.draw();
        messageBoardWidget.draw();
        criticalMessageBoardWidget.draw();
    }

    public void update(SimulationState simulationState) {
        debug.update();
        if(!drawGui) return;
        radar.update(simulationState);
        artificialHorizon.update(simulationState);
        propellersDisplay.update();
        map.update();
        projectiles.update(simulationState);
    }

    public void openMap(boolean open) {
        map.setHidden(!open);
    }
}
