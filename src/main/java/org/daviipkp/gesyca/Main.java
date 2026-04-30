package org.daviipkp.gesyca;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.StringJoiner;

import org.daviipkp.gesyca.DTOs.MachineInfo;

import io.javalin.Javalin;

public class Main {

    private static Javalin server;
    private static int PORT = 4500;

    private static final String MAIN_FOLDER = System.getProperty("user.dir"); 
    private static final File HOSTS_FILE = new File(MAIN_FOLDER + File.separator +  "hosts.txt");
    private static boolean temp = false;

    public static void main( String[] args ) {
        System.out.println("Setting up...");
        initialize();
    }

    private static void initialize() {
        if(!HOSTS_FILE.exists()) {
            System.out.println("Arquivo 'hosts.txt' não encontrado. Criando...");
            try {
                HOSTS_FILE.createNewFile();
            } catch (IOException e) {
                System.out.println("Não foi possível criar o arquivo 'hosts.txt'. Tenha certeza que o programa tem permissão necessária ou crie o arquivo manualmente.");
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
                        System.out.println("IP da máquina adicionado ao 'hosts.txt' com sucesso.");
                    } catch (Exception e) {
                        System.out.println("Impossível adicionar o IP da máquina atual ao arquivo.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Não foi possível ler o arquivo 'hosts.txt'. Verifique permissões.");
            }
            
        }
        setupServer();
    }

    public static void setupServer() {
        server = Javalin.create(config -> {
            config.routes.get("/hosts", ctx -> {
                StringJoiner b = new StringJoiner(" ");
                Files.lines(HOSTS_FILE.toPath()).forEach((String s) -> {
                    b.add(s);
                });

                ctx.result(b.toString() + System.lineSeparator());
            });

            config.routes.post("/add", ctx -> {
                
            });

            config.routes.post("/boot", ctx -> {
                MachineInfo mi = ctx.bodyAsClass(MachineInfo.class);
                ctx.result();
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

}
