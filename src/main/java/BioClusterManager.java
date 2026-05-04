import java.util.List;
import java.util.ArrayList;

public class BioClusterManager {
    public List < String > processarClusters(List < Observation > obs, double raio, double threshold, boolean modoInter, int limiteSeguranca) {
        List < String > conexoes = new ArrayList < > ();
        if (obs.size() < 2) {
            return conexoes;
        }
        for (int i = 0; i < obs.size(); i++) {
            Observation o1 = obs.get(i);
            for (int j = i + 1; j < obs.size(); j++) {
                Observation o2 = obs.get(j);
                double dist = Math.sqrt(Math.pow(o1.getX() - o2.getX(), 2) + Math.pow(o1.getY() - o2.getY(), 2));
                if ((dist < raio && (o1.getEspecieId() == o2.getEspecieId() || modoInter)) && (o1.getSaude() > threshold || o1.getSaude() > threshold) && !(o1.isInvasora() && o2.isInvasora())) {
                    conexoes.add("Cluster:" + o1.getId() + "-" + o2.getId());
                }
                if (conexoes.size() >= limiteSeguranca) {
                    return conexoes;
                }
            }
        }
        return conexoes;
    }
}
class Observation {
    private int id, especieId;
    private double x, y, saude;
    private boolean invasora;
    public Observation(int id, int especieId, double x, double y, double saude, boolean invasora) {
        this.id = id;
        this.especieId = especieId;
        this.x = x;
        this.y = y;
        this.saude = saude;
        this.invasora = invasora;
    }
    public int getId() {
        return id;
    }
    public int getEspecieId() {
        return especieId;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getSaude() {
        return saude;
    }
    public boolean isInvasora() {
        return invasora;
    }
}