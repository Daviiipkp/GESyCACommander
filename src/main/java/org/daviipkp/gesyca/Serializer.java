package org.daviipkp.gesyca;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daviipkp.gesyca.DTOs.MachineInfo;

public class Serializer {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<MachineInfo> listHosts(File jsonFile) throws IOException {
        if (!jsonFile.exists() || jsonFile.length() == 0) {
            return new ArrayList<>();
        }
        Map<String, List<MachineInfo>> data = mapper.readValue(
                jsonFile,
                new TypeReference<Map<String, List<MachineInfo>>>() {}
        );

        return data.getOrDefault("content", new ArrayList<>());
    }
    public static void addHost(File jsonFile, MachineInfo newHost) throws IOException {
        List<MachineInfo> hosts = listHosts(jsonFile);

        if (!hosts.contains(newHost)) {
            hosts.add(newHost);
            saveHosts(jsonFile, hosts);
            System.out.println("Host adicionado com sucesso: " + newHost.hostname());
        } else {
            System.out.println("Host já existe no arquivo.");
        }
    }
    public static void removeHost(File jsonFile, int id) throws IOException {
        List<MachineInfo> hosts = listHosts(jsonFile);

        boolean removed = hosts.removeIf(host -> host.ID() == id);

        if (removed) {
            saveHosts(jsonFile, hosts);
            System.out.println("Host com ID " + id + " removido com sucesso.");
        } else {
            System.out.println("Nenhum host encontrado com o ID: " + id);
        }
    }

    public static void removeHost(File jsonFile, String ipAddress) throws IOException {
        List<MachineInfo> hosts = listHosts(jsonFile);

        boolean removed = hosts.removeIf(host -> host.ipAddress().equals(ipAddress));

        if (removed) {
            saveHosts(jsonFile, hosts);
            System.out.println("Host com IP " + ipAddress + " removido com sucesso.");
        } else {
            System.out.println("Nenhum host encontrado com o IP: " + ipAddress);
        }
    }

    public static void saveHosts(File jsonFile, List<MachineInfo> hosts) throws IOException {
        Map<String, List<MachineInfo>> data = new HashMap<>();
        data.put("content", hosts);
        
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, data);
    }
}