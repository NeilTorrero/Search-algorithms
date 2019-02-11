package BranchBoundServer;

import BackTrackingServer.Haversine;
import Json.Nodes;
import Json.Server;
import Json.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Comparator;
import java.util.PriorityQueue;

public class BranchboundServer {


    public static void main(String[] args) throws FileNotFoundException {
        Gson gson = new GsonBuilder().create();
        User[] users = gson.fromJson(new FileReader("Datasets/users.json"), User[].class);
        Nodes[] nodes = gson.fromJson(new FileReader("Datasets/nodes.json"), Nodes[].class);
        Server[] servers = gson.fromJson(new FileReader("Datasets/servers.json"), Server[].class);
        for (int c = 0; c < users.length; c++) {
            users[c].setId(c);
            users[c].setUbication();
        }
        BranchboundServer sd = new BranchboundServer(users,servers);
        Solution wins = sd.Branchbound();
        sd.printArray(wins.carrega);
        System.out.println("La distancia minima es : " + wins.minDistancia);
        System.out.println("La Actividad minima es : " + wins.minActividad);
    }

    private void printArray(int[] x) {
        for (int i = 0; i < x.length; i++) {
            System.out.println(x[i]);
        }
        System.out.println();
    }

    private User[] usuarios;
    private Server[] servidores;
    private double actualDistancia = 0;
    private int[] actividadActualServidores;
    private int puntero = 0;

    private class Solution implements Cloneable{
        public int[] carrega = new int[usuarios.length];
        public float minActividad = 999999;
        public double minDistancia = 999999;

        public void updateCarrega (int pos, int new_server){
            this.carrega[pos] = new_server;
        }
    }

    public BranchboundServer(User[] usuarios, Server[] servidores) {//Pasar UsersId Por Orden Mejor pero da igual
        this.usuarios = usuarios;
        this.servidores = servidores;
        actividadActualServidores = new int[servidores.length];
    }



    public Solution Branchbound() {
        Solution x = new Solution();
        Solution best = new Solution();
        PriorityQueue<Solution> live_nodes = new PriorityQueue<Solution>(usuarios.length, new Comparator<Solution>(){
            public int compare(Solution o1, Solution o2){
                Float difAct = o1.minActividad - o2.minActividad;
                Double difDist = o1.minDistancia - o2.minDistancia;
                return difAct.intValue()/difDist.intValue();
            }
        });

        while (live_nodes.size() != 0) {
            x = live_nodes.poll();
            for (Server server : servidores) {
                if (puntero == usuarios.length) {
                    best = minmax(best);
                } else {
                    if (is_promising(server, best)) {
                        x.updateCarrega(puntero, Integer.parseInt(server.getId()));
                        live_nodes.add(x);
                    }
                }
            }
            puntero++;
        }
        return best;
    }

    private float getDistanceActivity() {
        float minor = 99999 ,mayor =0;
        for (int i = 0; i < actividadActualServidores.length ; i++) {
            if (actividadActualServidores[i] > mayor){
                mayor = actividadActualServidores[i];
            }
            if (actividadActualServidores[i] < minor && actividadActualServidores[i] != 0){
                minor = actividadActualServidores[i];
            }
            if(actividadActualServidores[i] == 0){
                return 999999999;
            }
        }
        return Math.abs(mayor - minor);
    }

    private Solution minmax(Solution best){
        float aux = getDistanceActivity();
        if (aux < best.minActividad && actualDistancia < best.minDistancia) {
            best.minActividad = aux;
            best.minDistancia = actualDistancia;
        }
        return best;
    }

    private boolean is_promising(Server option, Solution best){
        return  actualDistancia + Haversine.distance(usuarios[puntero].getLatitude(),
                usuarios[puntero].getLongitude(), option.getLocation().get(0),
                option.getLocation().get(1)) < best.minDistancia;
    }
}