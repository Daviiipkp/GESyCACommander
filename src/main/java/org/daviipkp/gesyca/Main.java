package org.daviipkp.gesyca;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.daviipkp.gesyca.DTOs.MachineInfo;

import io.javalin.Javalin;

public class Main {

    private static Javalin server;
    private static int PORT = 4500;

    private static final String MAIN_FOLDER = System.getProperty("user.dir"); 
    private static final File HOSTS_FILE = new File(MAIN_FOLDER + File.separator +  "hosts.json");
    private static boolean temp = false;

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
                String ip = InetAddress.getLocalHost().getHostAddress();
                System.out.println("Detectado o IP da máquina como " + ip);
                Files.lines(HOSTS_FILE.toPath()).forEach((String ss) -> {
                    InetAddress inetAddress;

                    System.out.println("Detectado o IP da máquina como " + ip);
                    if(ip.equals(ss)) {
                        temp = true;
                    }
           
                });
                if(temp) {
                    try {
                        addHost(InetAddress.getLocalHost().getHostAddress());
                        System.out.println("IP da máquina adicionado ao 'hosts.json' com sucesso.");
                    } catch (Exception e) {
                        System.out.println("Impossível adicionar o IP da máquina atual ao arquivo.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Não foi possível ler o arquivo 'hosts.json'. Verifique permissões.");
            }
            
        }
        setupServer();
    }

    public static void setupServer() {
        server = Javalin.create(config -> {
            config.routes.get("/hosts", ctx -> {
                System.out.println("Received on /hosts");
                try {
                    List<MachineInfo> hosts = Serializer.listHosts(HOSTS_FILE);

                    Map<String, List<MachineInfo>> jsonResponse = Map.of("content", hosts);
                    
                    ctx.json(jsonResponse);
                    
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    ctx.status(500).result("Erro interno ao ler o arquivo de hosts.");
                }
                });

            config.routes.post("/add", ctx -> {
                
            });

            config.routes.post("/clear", ctx -> {
               HOSTS_FILE.delete();
               HOSTS_FILE.createNewFile();
               ctx.status(201); 
            });

            config.routes.post("/boot", ctx -> {
                System.out.println("Received on /boot");
                MachineInfo mi = ctx.bodyAsClass(MachineInfo.class);
                List<MachineInfo> hosts = Serializer.listHosts(HOSTS_FILE);
                MachineInfo toRem = null;
                for(MachineInfo m : hosts) {
                    if(m.macAddress().equalsIgnoreCase(mi.macAddress())) {
                        toRem = m;
                        break;
                    }
                    
                }
                hosts.remove(toRem);
                hosts.add(mi);
                Serializer.saveHosts(HOSTS_FILE, hosts);
                ctx.result("Success!!");
            });

            
        }).start(PORT);

         
        System.out.println("Server is running on port " + PORT);
        
    }

    private static void addHost(String arg0) throws IOException{
        String ip = arg0 + System.lineSeparator();
                Files.write(
                HOSTS_FILE.toPath(), 
                ip.getBytes(), 
                StandardOpenOption.APPEND);
    }

    // private static String hostsAsString() {
    //     try {
    //         StringJoiner b = new StringJoiner(" ");
    //         for(MachineInfo i : Serializer.listHosts(HOSTS_FILE)) {

    //         }
            
    
    //         return (b.toString() + System.lineSeparator());
    //     } catch (Exception e) {
    //         return "Impossible to read hosts.txt file";
    //     }
    // }

}
