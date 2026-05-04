import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BioClusterManagerTest {

    private BioClusterManager manager;

    @BeforeEach
    public void setUp() {
        manager = new BioClusterManager();
    }

    @Test
    public void testListaVaziaOuUnitaria() {
        List<Observation> vazia = Arrays.asList();
        List<Observation> unitaria = Arrays.asList(
            new Observation(1, 1, 0.0, 0.0, 10.0, false)
        );

        assertTrue(manager.processarClusters(vazia, 10.0, 5.0, false, 10).isEmpty());
        assertTrue(manager.processarClusters(unitaria, 10.0, 5.0, false, 10).isEmpty());
    }

    @Test
    public void testTC1_ConexaoValidaBase() {
        // A=T, B=T, C=F, D=T, E=F, F=F
        List<Observation> obs = Arrays.asList(
            new Observation(1, 1, 0.0, 0.0, 10.0, false),
            new Observation(2, 1, 3.0, 4.0, 10.0, false) // distância = 5.0
        );
        List<String> result = manager.processarClusters(obs, 6.0, 5.0, false, 10);
        assertEquals(1, result.size());
        assertEquals("Cluster:1-2", result.get(0));
    }

    @Test
    public void testTC2_DistanciaForaDoRaio() {
        // A=F
        List<Observation> obs = Arrays.asList(
            new Observation(1, 1, 0.0, 0.0, 10.0, false),
            new Observation(2, 1, 10.0, 10.0, 10.0, false) // distância = ~14.1
        );
        List<String> result = manager.processarClusters(obs, 6.0, 5.0, false, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testTC3_EspeciesDiferentesModoInterFalso() {
        // B=F, C=F
        List<Observation> obs = Arrays.asList(
            new Observation(1, 1, 0.0, 0.0, 10.0, false),
            new Observation(2, 2, 3.0, 4.0, 10.0, false)
        );
        List<String> result = manager.processarClusters(obs, 6.0, 5.0, false, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testTC4_EspeciesDiferentesModoInterVerdadeiro() {
        // B=F, C=T
        List<Observation> obs = Arrays.asList(
            new Observation(1, 1, 0.0, 0.0, 10.0, false),
            new Observation(2, 2, 3.0, 4.0, 10.0, false)
        );
        List<String> result = manager.processarClusters(obs, 6.0, 5.0, true, 10);
        assertEquals(1, result.size());
    }

    @Test
    public void testTC5_SaudeAbaixoThreshold() {
        // D=F (testando a falha no onde apenas o1 importa)
        List<Observation> obs = Arrays.asList(
            new Observation(1, 1, 0.0, 0.0, 3.0, false), // saúde abaixo
            new Observation(2, 1, 3.0, 4.0, 10.0, false)
        );
        List<String> result = manager.processarClusters(obs, 6.0, 5.0, false, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testTC6_AmbasInvasoras() {
        // E=T, F=T
        List<Observation> obs = Arrays.asList(
            new Observation(1, 1, 0.0, 0.0, 10.0, true),
            new Observation(2, 1, 3.0, 4.0, 10.0, true)
        );
        List<String> result = manager.processarClusters(obs, 6.0, 5.0, false, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testLimiteSeguranca() {
        List<Observation> obs = Arrays.asList(
            new Observation(1, 1, 0.0, 0.0, 10.0, false),
            new Observation(2, 1, 1.0, 1.0, 10.0, false),
            new Observation(3, 1, 2.0, 2.0, 10.0, false)
        );
        // esperaria formar 1-2, 1-3, 2-3. limitando a 1
        List<String> result = manager.processarClusters(obs, 10.0, 5.0, false, 1);
        assertEquals(1, result.size());
    }
}