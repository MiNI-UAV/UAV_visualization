package org.uav.processor;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.lwjgl.glfw.GLFW;
import org.uav.config.Config;
import org.uav.model.Drone;
import org.uav.model.SimulationState;
import org.uav.queue.DroneRequester;
import org.uav.queue.DroneStatusConsumer;
import org.uav.queue.NotificationsConsumer;
import org.uav.queue.ProjectileStatusesConsumer;
import org.uav.scene.LoadingScreen;
import org.zeromq.ZContext;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SimulationStateProcessor implements AutoCloseable {

    private static final String KILL_COMMAND = "kill";
    public static final String ASSETS_ARCHIVE = "/assets.tar.gz";
    private final SimulationState simulationState;
    private final Config config;
    private final DroneRequester droneRequester;
    private final DroneStatusConsumer droneStatusConsumer;
    private final ProjectileStatusesConsumer projectileStatusesConsumer;
    private final NotificationsConsumer notificationsConsumer;


    public SimulationStateProcessor(SimulationState simulationState, Config config) {
        this.simulationState = simulationState;
        this.config = config;
        var context = new ZContext();
        droneRequester = new DroneRequester(context, simulationState, config);
        droneStatusConsumer = new DroneStatusConsumer(context, simulationState, config);
        projectileStatusesConsumer = new ProjectileStatusesConsumer(context, simulationState, config);
        notificationsConsumer = new NotificationsConsumer(context, config, simulationState);
    }

    public void openCommunication() {
        droneStatusConsumer.start();
        projectileStatusesConsumer.start();
        notificationsConsumer.start();
    }

    public void requestNewDrone() {
        var newDroneResult = droneRequester.requestNewDrone(config.getDroneSettings().getDroneName(), simulationState.getDroneModelChecksum());
        simulationState.setCurrentlyControlledDrone(newDroneResult.orElseThrow());
    }

    public void updateCurrentEntityStatuses() {
        simulationState.getDroneStatusesMutex().lock();
        simulationState.getCurrPassDroneStatuses().map = simulationState.getDroneStatuses().map;
        simulationState.getDroneStatusesMutex().unlock();
        simulationState.getProjectileStatusesMutex().lock();
        simulationState.getCurrPassProjectileStatuses().map = simulationState.getProjectileStatuses().map;
        simulationState.getProjectileStatusesMutex().unlock();
        simulationState.setSimulationTime((float) GLFW.glfwGetTime());
    }

    @Override
    public void close() {
        droneStatusConsumer.stop();
        projectileStatusesConsumer.stop();
        notificationsConsumer.interrupt();
    }

    public void respawnDrone() {
        Drone oldDrone = simulationState.getCurrentlyControlledDrone();
        requestNewDrone();
        oldDrone.sendUtilsCommand(KILL_COMMAND);
        simulationState.setCurrentControlMode(config.getDroneSettings().getDefaultControlMode());
    }

    public void checkAndUpdateAssets(Config config, SimulationState simulationState, LoadingScreen loadingScreen) throws IOException {
        var serverInfo = droneRequester.fetchServerInfo();
        if(config.getServerSettings().getAssetsToUse() != null)
            simulationState.setAssetsDirectory(Paths.get(System.getProperty("user.dir"), "assets", config.getServerSettings().getAssetsToUse()).toString());
        else
            simulationState.setAssetsDirectory(Paths.get(System.getProperty("user.dir"), "assets", serverInfo.assetChecksum.substring(0,8)).toString());
        simulationState.setServerMap(serverInfo.serverMap);

        if(
                config.getServerSettings().isDownloadMissingAssets()
                && !assetPackExists(serverInfo.assetChecksum.substring(0,8))
        ) {
            loadingScreen.render("Downloading new assets...");
            downloadAssets(config.getServerSettings().getAssetsSourceUrl(), serverInfo.assetChecksum, loadingScreen);
        }
    }

    private void downloadAssets(String assetsSourceUrl, String assetChecksum, LoadingScreen loadingScreen) throws IOException {
        String assetPackDirectory = Paths.get(System.getProperty("user.dir"), "assets", assetChecksum.substring(0,8)).toString();
        if(!new File(assetPackDirectory).mkdir()) throw new IOException();
        String saveAtPath = Paths.get(assetPackDirectory, ASSETS_ARCHIVE).toString();
        String downloadUrlPath = assetsSourceUrl + assetChecksum + ASSETS_ARCHIVE;
        URL downloadUrl = new URL(downloadUrlPath);
        HttpURLConnection httpConnection = (HttpURLConnection) (downloadUrl.openConnection());
        long completeFileSize = httpConnection.getContentLength();
        float fileSizeMb = (float) (completeFileSize / 1000_00) / 10;

        try(InputStream inputStream = downloadUrl.openStream();
            CountingInputStream cis = new CountingInputStream(inputStream);
            FileOutputStream fileOS = new FileOutputStream(saveAtPath)
        ) {

            new Thread(() -> {
                try {
                    IOUtils.copyLarge(cis, fileOS);
                } catch (IOException e) {
                    throw new RuntimeException();
                }
            }).start();

            int byteStep = 100_000;
            long lastStep = 0;
            while (cis.getByteCount() < completeFileSize) {
                if (cis.getByteCount() - lastStep > byteStep) {
                    lastStep += byteStep;
                    float currentMb = (float) (cis.getByteCount() / 100_000) / 10;
                    loadingScreen.render("Downloading new assets... " + currentMb + "/" + fileSizeMb + " MB");
                }
            }
        }

        loadingScreen.render("Unpacking assets...");
        unTar(saveAtPath, assetPackDirectory);
        File unTarredDirectory = new File(Paths.get(assetPackDirectory, "assets").toString());
        for(File file: unTarredDirectory.listFiles())
            FileUtils.moveDirectoryToDirectory(file, new File(assetPackDirectory), false);
        unTarredDirectory.delete();
        new File(saveAtPath).delete();
    }

    public void unTar(String origin, String destination) throws IOException {
        try (InputStream inputStream = new FileInputStream(origin);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
             TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(bufferedInputStream))) {
            ArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                Path extractTo = Path.of(destination).resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(extractTo);
                } else {
                    Files.copy(tar, extractTo);
                }
            }
        }
    }

    private boolean assetPackExists(String assetChecksum) throws IOException {
        String assetsDirectory = Paths.get(System.getProperty("user.dir"), "assets").toString();
        if(!Files.exists(Paths.get(assetsDirectory))) {
            if(!new File(assetsDirectory).mkdir()) throw new IOException();
            return false;
        }
        String assetPackDirectory = Paths.get(assetsDirectory, assetChecksum).toString();
        return Files.exists(Paths.get(assetPackDirectory));
    }

    public void saveDroneModelChecksum(String droneModel) {
        String droneModelConfigPath = Paths.get(System.getProperty("user.dir"), "drones", droneModel + ".xml").toString();
        String droneModelChecksum = droneRequester.sendConfigFile(droneModelConfigPath);
        simulationState.setDroneModelChecksum(droneModelChecksum);
    }
}
