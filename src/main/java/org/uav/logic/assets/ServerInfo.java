package org.uav.logic.assets;

import java.util.ArrayList;
import java.util.List;

public class ServerInfo {
    public String assetChecksum;
    public String serverMap;
    public List<String> configs;

    public ServerInfo(String assetChecksum, String serverMap, ArrayList<String> configs) {
        this.assetChecksum = assetChecksum;
        this.serverMap = serverMap;
        this.configs = configs;
    }
}
