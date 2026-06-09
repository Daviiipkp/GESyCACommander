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
import org.daviipkp.gesyca.DTOs.MachineInfoWithID;

public class Serializer {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Map<Integer,MachineInfo> listHosts(File jsonFile) throws IOException {
        if (!jsonFile.exists() || jsonFile.length() == 0) {
            return new HashMap<Integer,MachineInfo>();
        }
        Map<String, Map<Integer,MachineInfo>> data = mapper.readValue(
                jsonFile,
                new TypeReference<Map<String, Map<Integer,MachineInfo>>>() {}
        );

        return data.getOrDefault("content", new HashMap<Integer,MachineInfo>());
    }
    public static void addHost(File jsonFile, MachineInfoWithID newHost) throws IOException {
        Map<Integer,MachineInfo> hosts = listHosts(jsonFile);

        if (!hosts.containsKey(newHost)) {
            hosts.put(newHost.ID(), new MachineInfo(newHost.hostname(), newHost.ipAddress(), newHost.macAddress()));
            saveHosts(jsonFile, hosts);
            System.out.println("Host adicionado com sucesso: " + newHost.hostname());
        } else {
            System.out.println("Host já existe no arquivo.");
        }
    }
    public static void removeHost(File jsonFile, int id) throws IOException {
        Map<Integer,MachineInfo> hosts = listHosts(jsonFile);

        if(hosts.containsKey(id)) {
            hosts.remove(id);
            saveHosts(jsonFile, hosts);
            System.out.println("Host com ID " + id + " removido com sucesso.");
        } else {
            System.out.println("Nenhum host encontrado com o ID: " + id);
        }
    }

    public static void removeHost(File jsonFile, String ipAddress) throws IOException {
        Map<Integer,MachineInfo> hosts = listHosts(jsonFile);
        Integer keyToRemove = null;
        for(Integer in : hosts.keySet()) {
            if(hosts.get(in).ipAddress().equals(ipAddress)) {
                keyToRemove = in;
            }
        }

        if(keyToRemove != null) {
            hosts.remove(keyToRemove);
            saveHosts(jsonFile, hosts);
            System.out.println("Host com IP " + ipAddress + " removido com sucesso.");
        } else {
            System.out.println("Nenhum host encontrado com o IP: " + ipAddress);
        }
    }

    public static void saveHosts(File jsonFile, Map<Integer,MachineInfo> hosts) throws IOException {
        Map<String, Map<Integer,MachineInfo>> data = new HashMap<>();
        data.put("content", hosts);
        
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, data);
    }
}