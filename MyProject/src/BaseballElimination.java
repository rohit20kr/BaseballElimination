import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseballElimination {
    private final int numberOfTeams;
    private final Map<String, Integer> teamToId;
    private final Map<Integer, String> idToTeam;
    private final int[] wins;
    private final int[] losses;
    private final int[] remaining;
    private final int[][] against;

    public BaseballElimination(String filename) {
        In in = new In(filename);
        numberOfTeams = in.readInt();
        teamToId = new HashMap<>();
        idToTeam = new HashMap<>();
        wins = new int[numberOfTeams];
        losses = new int[numberOfTeams];
        remaining = new int[numberOfTeams];
        against = new int[numberOfTeams][numberOfTeams];

        int id = 0;
        while (!in.isEmpty()) {
            String team = in.readString();
            teamToId.put(team, id);
            idToTeam.put(id, team);
            wins[id] = in.readInt();
            losses[id] = in.readInt();
            remaining[id] = in.readInt();
            for (int i = 0; i < numberOfTeams; i++) {
                against[id][i] = in.readInt();
            }
            id++;
        }
    }

    public int numberOfTeams() {
        return numberOfTeams;
    }

    public Iterable<String> teams() {
        return teamToId.keySet();
    }

    public int wins(String team) {
        validateTeam(team);
        return wins[teamToId.get(team)];
    }

    public int losses(String team) {
        validateTeam(team);
        return losses[teamToId.get(team)];
    }

    public int remaining(String team) {
        validateTeam(team);
        return remaining[teamToId.get(team)];
    }

    public int against(String team1, String team2) {
        validateTeam(team1);
        validateTeam(team2);
        return against[teamToId.get(team1)][teamToId.get(team2)];
    }

    public boolean isEliminated(String team) {
        validateTeam(team);
        int teamId = teamToId.get(team);

        // Trivial elimination
        for (int i = 0; i < numberOfTeams; i++) {
            if (wins[teamId] + remaining[teamId] < wins[i]) {
                return true;
            }
        }

        // Non-trivial elimination
        FlowNetwork flowNetwork = buildFlowNetwork(team);
        FordFulkerson maxFlow = new FordFulkerson(flowNetwork, 0, flowNetwork.V() - 1);

        for (FlowEdge edge : flowNetwork.adj(0)) {
            if (edge.flow() < edge.capacity()) {
                return true;
            }
        }

        return false;
    }

    public Iterable<String> certificateOfElimination(String team) {
        validateTeam(team);
        int teamId = teamToId.get(team);

        // Trivial elimination
        List<String> trivialElimination = new ArrayList<>();
        for (int i = 0; i < numberOfTeams; i++) {
            if (wins[teamId] + remaining[teamId] < wins[i]) {
                trivialElimination.add(idToTeam.get(i));
            }
        }

        if (!trivialElimination.isEmpty()) {
            return trivialElimination;
        }

        // Non-trivial elimination
        FlowNetwork flowNetwork = buildFlowNetwork(team);
        FordFulkerson maxFlow = new FordFulkerson(flowNetwork, 0, flowNetwork.V() - 1);

        List<String> certificate = new ArrayList<>();
        for (int i = 0; i < numberOfTeams; i++) {
            if (i != teamId && maxFlow.inCut(i + 1)) {
                certificate.add(idToTeam.get(i));
            }
        }

        if (certificate.isEmpty()) {
            return null;
        }

        return certificate;
    }

    private FlowNetwork buildFlowNetwork(String team) {
        int teamId = teamToId.get(team);
        int numVertices = numberOfTeams + 2;
        int gameVertices = numberOfTeams * (numberOfTeams - 1) / 2;
        int vertexId = 1;

        FlowNetwork flowNetwork = new FlowNetwork(numVertices);

        for (int i = 0; i < numberOfTeams; i++) {
            if (i != teamId) {
                for (int j = i + 1; j < numberOfTeams; j++) {
                    if (j != teamId) {
                        int capacity = against[i][j];
                        flowNetwork.addEdge(new FlowEdge(0, vertexId, capacity));
                        flowNetwork.addEdge(new FlowEdge(vertexId, i + 1, Double.POSITIVE_INFINITY));
                        flowNetwork.addEdge(new FlowEdge(vertexId, j + 1, Double.POSITIVE_INFINITY));
                        vertexId++;
                    }
                }
            }
        }

        int teamVertex = numVertices - 1;
        int maxPossibleWins = wins[teamId] + remaining[teamId];

        for (int i = 0; i < numberOfTeams; i++) {
            if (i != teamId) {
                flowNetwork.addEdge(new FlowEdge(i + 1, teamVertex, maxPossibleWins - wins[i]));
            }
        }

        return flowNetwork;
    }

    private void validateTeam(String team) {
        if (!teamToId.containsKey(team)) {
            throw new IllegalArgumentException("Team not found.");
        }
    }

    public static void main(String[] args) {
        String filename = "C:\\Users\\rohit\\Desktop\\MyProject\\src\\team5.txt";
        BaseballElimination division = new BaseballElimination(filename);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
