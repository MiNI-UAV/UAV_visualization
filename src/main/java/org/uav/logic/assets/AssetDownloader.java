package org.uav.logic.assets;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.uav.logic.config.Config;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.view.LoadingScreen;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.uav.utils.ZmqUtils.checkErrno;

public class AssetDownloader {
    public static final String ASSETS_ARCHIVE = "/assets.tar.gz";
    private final ZMQ.Socket socket;

    public AssetDownloader(ZContext context, Config config) {
        String address = "tcp://" + config.getServerSettings().getServerAddress() + ":" + config.getPorts().getDroneRequester();
        socket = context.createSocket(SocketType.REQ);
        socket.setSendTimeOut(config.getServerSettings().getServerTimeoutMs());
        socket.setReceiveTimeOut(config.getServerSettings().getServerTimeoutMs());
        socket.connect(address);
    }

    public void checkAndUpdateAssets(Config config, SimulationState simulationState, LoadingScreen loadingScreen) throws IOException {
        var serverInfo = fetchServerInfo();
        if(config.getServerSettings().getAssetsToUse() != null)
            simulationState.setAssetsDirectory(Paths.get(System.getProperty("user.dir"), "assets", config.getServerSettings().getAssetsToUse()).toString());
        else
            simulationState.setAssetsDirectory(Paths.get(System.getProperty("user.dir"), "assets", serverInfo.assetChecksum.substring(0,8)).toString());
        simulationState.setServerMap(serverInfo.serverMap);

        if(
                config.getServerSettings().getDownloadMissingAssets()
                        && !assetPackExists(serverInfo.assetChecksum.substring(0,8))
        ) {
            loadingScreen.render("Downloading new assets...");
            downloadAssets(config.getServerSettings().getAssetsSourceUrl(), serverInfo.assetChecksum, loadingScreen);
        }
    }

    public ServerInfo fetchServerInfo() {
        if(!socket.send(("i").getBytes(ZMQ.CHARSET), 0)) checkErrno(socket);
        byte[] reply = socket.recv();
        if(reply == null) checkErrno(socket);
        String message = new String(reply, ZMQ.CHARSET);
        JSONObject obj = new JSONObject(message);
        String assetChecksum = obj.getString("checksum");
        String serverMap = obj.getString("map");
        var configs = new ArrayList<String>();
        JSONArray arr = obj.getJSONArray("configs");
        for (int i = 0; i < arr.length(); i++)
            configs.add(arr.getString(i));
        return new ServerInfo(assetChecksum, serverMap, configs);
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
}
