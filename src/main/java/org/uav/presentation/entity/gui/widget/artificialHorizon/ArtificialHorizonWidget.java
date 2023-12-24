package org.uav.presentation.entity.gui.widget.artificialHorizon;

import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.uav.logic.config.Config;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.gui.GuiAnchorPoint;
import org.uav.presentation.entity.gui.Widget;
import org.uav.presentation.entity.sprite.Sprite;
import org.uav.presentation.rendering.Shader;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.uav.utils.IOUtils.loadImage;

public class ArtificialHorizonWidget extends Widget {
    private final ArtificialHorizonBackgroundLayer backgroundLayer;
    private final ArtificialHorizonCompassLayer compassLayer;
    private final ArtificialHorizonRollLayer rollLayer;
    private final ArtificialHorizonMetersLayer metersLayer;

    private final Sprite cursorSprite;

    public ArtificialHorizonWidget(Path assetsDirectory, Shader spriteShader, Shader vectorShader, Shader textShader, Config config) {
        super(getWidgetPosition(), GuiAnchorPoint.BOTTOM, config);
        BufferedImage horizonTexture = loadImage(Paths.get(assetsDirectory.toString(), "horizon.png").toString());
        BufferedImage cursorTexture = loadImage(Paths.get(assetsDirectory.toString(), "horizonCursor.png").toString());
        BufferedImage horizonRollTexture = loadImage(Paths.get(assetsDirectory.toString(), "horizonRoll.png").toString());
        BufferedImage compassTexture = loadImage(Paths.get(assetsDirectory.toString(), "compass.png").toString());

        int distanceToMax = 360;
        int horizonScreenX = 480;
        int horizonScreenY = 360;
        backgroundLayer = new ArtificialHorizonBackgroundLayer(horizonTexture, distanceToMax, horizonScreenX, horizonScreenY, spriteShader, vectorShader);
        compassLayer = new ArtificialHorizonCompassLayer(compassTexture, horizonScreenX, spriteShader, vectorShader);
        rollLayer = new ArtificialHorizonRollLayer(horizonRollTexture, spriteShader, vectorShader, getViewRatio());
        metersLayer = new ArtificialHorizonMetersLayer(horizonScreenX, horizonScreenY, getScaledPosition(), vectorShader, textShader, config);
        cursorSprite = new Sprite(cursorTexture, spriteShader);
    }

    private static Vector4f getWidgetPosition() {
        return new Vector4f(-0.4f, -1.0f, -0.4f, 0.4f);
    }

    public void update(SimulationState simulationState) {
        backgroundLayer.update(simulationState);
        compassLayer.update(simulationState);
        rollLayer.update(simulationState);
        metersLayer.update(simulationState);
    }
    @Override
    protected void drawWidget(MemoryStack stack) {
        backgroundLayer.draw(stack);
        compassLayer.draw(stack);
        rollLayer.draw(stack);
        cursorSprite.draw(stack);
        metersLayer.draw(stack);
    }
}
