package org.daviipkp.gesyca;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.daviipkp.gesyca.DTOs.CommandInfo;
import org.daviipkp.gesyca.DTOs.MachineInfo;
import org.daviipkp.gesyca.DTOs.MachineInfoWithID;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import io.javalin.Javalin;

public class Main {

    private static Javalin server;
    private static int PORT = 4500;

    private static final String MAIN_FOLDER = System.getProperty("user.dir"); 
    private static final File HOSTS_FILE = new File(MAIN_FOLDER + File.separator +  "hosts.json");

    public static void main( String[] args ) {
        System.out.println("Setting up...");
        initialize();
    }

    private static void initialize() {
        if(!HOSTS_FILE.exists()) {
            System.out.println("Arquivo 'hosts.json' não encontrado. Criando...");
            try {
                HOSTS_FILE.createNewFile();
            } catch (IOException e) {
                System.out.println("Não foi possível criar o arquivo 'hosts.json'. Tenha certeza que o programa tem permissão necessária ou crie o arquivo manualmente.");
            }
        }else{
            try {
                String ip = getRealIP();
                System.out.println("Detectado o IP da máquina como " + ip);
                Map<Integer, MachineInfo> hosts = Serializer.listHosts(HOSTS_FILE);
                for(Integer i : hosts.keySet()) {
                    if(ip.equals(hosts.get(i).ipAddress())) {
                        Serializer.removeHost(HOSTS_FILE, ip);
                    }
                    if(getMac().equals(hosts.get(i).macAddress())) {
                        Serializer.removeHost(HOSTS_FILE, ip);
                    }
                }
                try {
                    
                    Serializer.addHost(HOSTS_FILE, new MachineInfoWithID(InetAddress.getLocalHost().getHostName(), ip, 1, getMac()));
                    System.out.println("IP da máquina adicionado ao 'hosts.json' com sucesso.");
                } catch (Exception e) {
                    System.out.println("Impossível adicionar o IP da máquina atual ao arquivo. Stacktrace: ");
                    e.printStackTrace();
                }
                
            } catch (IOException e) {
                System.out.println("Não foi possível ler o arquivo 'hosts.json'. Verifique permissões. stacktrace: ");
                e.printStackTrace();
            }
            
        }
        setupServer();
    }

    public static String getRealIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    if (addr instanceof java.net.Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "IP não encontrado";
    }

    public static String getMac() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                
                if (!network.isUp() || network.isLoopback() || network.isVirtual()) {
                    continue;
                }
                
                byte[] mac = network.getHardwareAddress();
                
                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao tentar obter o MAC: " + e.getMessage());
        }
        
        return "NOT FOUND!";
    }

    public static void setupServer() {
        server = Javalin.create(config -> {
            config.routes.get("/hostsr", ctx -> {
                System.out.println("Received on /hostsr");
                try {
                    Map<Integer, MachineInfo> hosts = Serializer.listHosts(HOSTS_FILE);

                    Map<String, Map<Integer, MachineInfo>> jsonResponse = Map.of("content", hosts);
                    
                    ctx.json(jsonResponse);
                    
                    ctx.status(201); 
                } catch (IOException e) {
                    e.printStackTrace();
                    ctx.status(500).result("Erro interno ao ler o arquivo de hosts.");
                }
                });

            config.routes.get("/hostsf", ctx -> {
                System.out.println("Received on /hostsf");
                try {
                    Map<Integer, MachineInfo> hosts = Serializer.listHosts(HOSTS_FILE);

                    StringBuilder b = new StringBuilder();
                    for(int i = 1; i < 11; i++) {
                       if(hosts.containsKey(i)) {
                            b.append("Machine with ID " + i + "\n");
                            b.append("Machine with IP Address " + hosts.get(i).ipAddress() + "\n");
                            b.append("Machine with hostname " + hosts.get(i).hostname() + "\n");
                            b.append("Machine with MAC Address " + hosts.get(i).macAddress() + "\n");
                            b.append("=======================" + "\n");
                       }
                    }
                    
                    ctx.result(b.toString());
                    
                    ctx.status(201); 
                } catch (IOException e) {
                    e.printStackTrace();
                    ctx.status(500).result("Erro interno ao ler o arquivo de hosts.");
                }
                });

            config.routes.post("/command/{id}", ctx -> {
                String pathId = ctx.pathParam("id");
                int i = -1;
                try {
                    i = Integer.parseInt(pathId);
                } catch (NumberFormatException e) {
                    if(!pathId.equalsIgnoreCase("all")) {
                        ctx.result("Usage error! Expected /command/{id} or /command/all.");
                        return;
                    } 
                }

                CommandInfo b = ctx.bodyAsClass(CommandInfo.class);
                StringBuilder sb = new StringBuilder();
                Map<Integer, MachineInfo> machines = Serializer.listHosts(HOSTS_FILE);
                if(i == -1) {
                    for(int id : machines.keySet()) {
                        try {
                            sb.append(sendCommand(b.username(), machines.get(id).ipAddress(), b.pass(), b.cmd(), id));
                        } catch (Exception ee) {
                            ctx.result("Error trying to send to machine with id " + id + ". Error message: " + ee.getMessage());
                        }
                    }
                    ctx.result(sb.toString());
                } else{
                    try {
                        ctx.result(sendCommand(b.username(), machines.get(i).ipAddress(), b.pass(), b.cmd(), i));
                    } catch (NullPointerException e) {
                        ctx.result("Machine with id " + i + " is not registered");
                    }
                }
                
            });

            config.routes.post("/clear", ctx -> {
               HOSTS_FILE.delete();
               HOSTS_FILE.createNewFile();
               ctx.status(201); 
            });

            config.routes.post("/boot", ctx -> {
                MachineInfoWithID mi = ctx.bodyAsClass(MachineInfoWithID.class);
                System.out.println("Received on /boot from machine with ID " + mi.ID() + " and IP Address " + mi.ipAddress());
                Map<Integer,MachineInfo> hosts = Serializer.listHosts(HOSTS_FILE);
                Integer toRem = null;
                for(Integer m : hosts.keySet()) {
                    if(hosts.get(m).macAddress().equalsIgnoreCase(mi.macAddress())) {
                        toRem = m;
                        break;
                    }
                    
                }
                hosts.remove(toRem);
                hosts.put(mi.ID(),new MachineInfo(mi.hostname(), mi.ipAddress(), mi.macAddress()));
                Serializer.saveHosts(HOSTS_FILE, hosts);
                ctx.status(201); 
            });

            
        }).start(PORT);

         
        System.out.println("Server is running on port " + PORT);
        
    }

    public static String sendCommand(String user, String host, String password, String command, int ID) {
        JSch j = new JSch();
        Session session = null;
        ChannelExec c = null;
        StringBuilder b = new StringBuilder();
        try {
            session = j.getSession(user, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");

            session.connect();
            c = (ChannelExec) session.openChannel("exec");
            
            c.setCommand(command);

            InputStream in = c.getInputStream();
            InputStream err = c.getErrStream();

            c.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            b.append("");
            b.append("--- Output from machine with ID " + ID + " ---");
            while ((line = reader.readLine()) != null) {
                b.append(line);
            }
            b.append("");


            BufferedReader errReader = new BufferedReader(new InputStreamReader(err));
            while ((line = errReader.readLine()) != null) {
                b.append("Err: " + line);
            }

            if (c.isClosed()) {
               b.append("Exit status: " + c.getExitStatus());
            }

            

        } catch (Exception e) {
            b.append("Error on machine with ID " + ID + ", error message: " + e.getMessage());
        } finally {
            if (c != null && c.isConnected()) {
                c.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
            b.append("Ended connection to machine with ID " + ID);
        }
        return b.toString();
    }

}
