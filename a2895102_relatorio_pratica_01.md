---
title: Prática 01 - Prática de Teste Estrutural e Teste de Mutação
author: Gustavo Henrique Morais da Silva
RA: a2895102
date: 01/05/2026
---

## 1. Teste baseado em especificação

Analisado a especificação para identificar os parâmetros de entrada e suas classes de equivalência e valores limite.

### 1.1 Identificação das partições e limites

| Parâmetro / Condição                         | Classe Válida (In-point)              | Classe Inválida / Limite (Out-point)           |
| :------------------------------------------- | :------------------------------------ | :--------------------------------------------- |
| **Quantidade de Observações (`obs.size()`)** | `>= 2` (Permite formar cluster)       | `< 2` (0 ou 1 observação - Retorna vazio)      |
| **Distância Euclidiana (`dist`)**            | `< raio`                              | `>= raio`                                      |
| **Identidade Taxonômica (`especieId`)**      | `o1.especieId == o2.especieId`        | `o1.especieId != o2.especieId`                 |
| **Modo Interespécies (`modoInter`)**         | `true` (Ignora restrição de espécie)  | `false` (Aplica restrição de espécie)          |
| **Saúde dos Espécimes (`saude`)**            | Ambos `> threshold`                   | Um ou ambos `<= threshold`                     |
| **Segurança Biológica (`invasora`)**         | Apenas uma invasora; Nenhuma invasora | Ambas invasoras simultaneamente (`true, true`) |
| **Limite de Segurança (`limiteSeguranca`)**  | `conexoes.size() < limiteSeguranca`   | `conexoes.size() >= limiteSeguranca`           |

## 2. Teste estrutural

A análise estrutural foca na cobertura do código-fonte. A principal método `processarClusters` está com a seguinte estrutura condicional:

```java
if ((dist < raio && (o1.getEspecieId() == o2.getEspecieId() || modoInter)) &&
    (o1.getSaude() > threshold || o1.getSaude() > threshold) &&
    !(o1.isInvasora() && o2.isInvasora()))
```

### 2.1 Erros na implementação

Dois erros foram identificados na implementação da condicional:

1. **Validação de Saúde Duplicada (Erro de Copiar/Colar):** A expressão `(o1.getSaude() > threshold || o1.getSaude() > threshold)` verifica a saúde do objeto `o1` duas vezes e ignora completamente a saúde do objeto `o2`. Além disso, a especificação diz "indivíduos que possuam índices de saúde satisfatórios", o que implica um operador lógico `&&` (E), e não `||` (OU).
2. **Loop de Limite de Segurança:** O retorno antecipado `if (conexoes.size() >= limiteSeguranca)` está dentro do loop interno. Isso funciona, mas a condição do loop não previne iterações desnecessárias no loop externo.

### 2.2 Tabela verdade MC/DC

Para derivar o MC/DC, abstraído a condicional complexa. Testado a lógica considerando as variáveis independentes para demonstrar a falha.

Decisão = `A && (B || C) && (D) && !(E && F)`

- **A:** `dist < raio`
- **B:** `mesmaEspecie`
- **C:** `modoInter`
- **D:** `o1.saude > threshold`
- **E:** `o1.invasora`
- **F:** `o2.invasora`

_Tabela de pares de independência (Resumo para Cobertura)_:
Para garantir MC/DC, cada condição demonstrar sua capacidade de alterar o resultado da decisão, mantendo as demais fixas.

| ID Teste | A (Dist) | B (Espécie) | C (Modo) | D (Saúde) | E (Inv 1) | F (Inv 2) | Resultado | Avalia                  |
| :------- | :------- | :---------- | :------- | :-------- | :-------- | :-------- | :-------- | :---------------------- |
| **TC1**  | T        | T           | F        | T         | F         | F         | **TRUE**  | Base Válida             |
| **TC2**  | F        | T           | F        | T         | F         | F         | **FALSE** | A (Falso)               |
| **TC3**  | T        | F           | F        | T         | F         | F         | **FALSE** | B (Falso)               |
| **TC4**  | T        | F           | T        | T         | F         | F         | **TRUE**  | C (Verdadeiro)          |
| **TC5**  | T        | T           | F        | F         | F         | F         | **FALSE** | D (Falso)               |
| **TC6**  | T        | T           | F        | T         | T         | T         | **FALSE** | E e F (Ambas invasoras) |

![Execução do comando mvn clean test para gerar o relatório do Jacoco](https://github.com/gustavomoraiis/teste-de-software/blob/main/jacoco-terminal.png)
![Relatório do Jacoco](https://github.com/gustavomoraiis/teste-de-software/blob/main/jacoco-report.png)

## 3. Teste baseado em mutação

A ferramenta PIT (PIT Mutation Testing) introduz falhas artificiais (mutantes) no código, como trocar `<` por `<=`, ou `&&` por `||`. Objetivo do testes é remover esses mutantes.

O defeito na linha da saúde `(o1... || o1...)`, mutantes que alteram a avaliação do objeto `o2` provavelmente permaneceram, o que reforça o achado da etapa estrutural.

![Execução do comando mvn test-compile org.pitest:pitest-maven:mutationCoverage para gerar o relatório do PIT - Score de Mutação](https://github.com/gustavomoraiis/teste-de-software/blob/main/pit-report.png)
![Relatório PIT BreakDown da classe BioClusterManager](https://github.com/gustavomoraiis/teste-de-software/blob/main/pit-breakdown-bioclustermanager.png)

## 4. Testes implementados com JUnit

Utilização do JUnit desenvolvida para cobrir as especificações e a tabela MC/DC.

```java
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
            new Observation(2, 1, 3.0, 4.0, 10.0, false) // distancia = 5.0
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
            new Observation(2, 1, 10.0, 10.0, 10.0, false) // distancia = ~14.1
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
        // D=F
        List<Observation> obs = Arrays.asList(
            new Observation(1, 1, 0.0, 0.0, 3.0, false), // saude abaixo
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
        // formar 1-2, 1-3, 2-3. limitando a 1
        List<String> result = manager.processarClusters(obs, 10.0, 5.0, false, 1);
        assertEquals(1, result.size());
    }
}
```
