import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        Grammar grammar = readGrammarFromFile("src/gramatica.txt");

        System.out.println("Gramatică citită din fișier:");
        System.out.println("Simbol de start: " + grammar.startSymbol);
        System.out.println("Producții:");
        for (Production p : grammar.productions) {
            System.out.println(p);
        }

        // Creează harta tranzițiilor și generează toate stările LR(1).
        Map<State, Map<String, State>> transitions = new HashMap<>();
        List<State> states = generateStates(grammar, transitions); // Generate all LR(1) states.

        // Construiește liste separate pentru terminale și neterminale.
        List<String> terminals = new ArrayList<>(grammar.terminals);
        terminals.add("$");
        List<String> nonTerminals = new ArrayList<>(grammar.nonTerminals);

        Map<State, Map<String, String>> actionTable = generateActionTable(states, grammar, transitions);
        Map<State, Map<String, Integer>> gotoTable = generateGotoTable(states, grammar, transitions);

        printActionTable(states, actionTable, terminals);
        printGotoTable(states, gotoTable, nonTerminals);
    }

    private static Grammar readGrammarFromFile(String filename) throws IOException {
        Grammar grammar = new Grammar("S"); // Start symbol is "S" as provided.
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim(); // Elimină spațiile goale.
            if (!line.isEmpty()) {
                String[] parts = line.split("->");
                String left = parts[0].trim();
                String right = parts[1].trim();
                grammar.addProduction(left, right);
            }
        }

        reader.close();
        return grammar;
    }

    private static List<State> generateStates(Grammar grammar, Map<State, Map<String, State>> transitions) {
        // Listează toate stările și inițializează starea de început.
        List<State> states = new ArrayList<>();
        State startState = closure(Set.of(new LR1Item(grammar.startSymbol, List.of("E"), 0, "$")), grammar);
        states.add(startState);

        // Folosește o coadă pentru a procesa fiecare stare.
        Queue<State> queue = new LinkedList<>();
        queue.add(startState);

        // Parcurge toate stările posibile pentru a genera tranzițiile.c
        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            Map<String, State> currentTransitions = new HashMap<>();
            transitions.put(currentState, currentTransitions);

            // Identifică simbolurile care pot fi procesate în această stare.
            Set<String> symbols = new HashSet<>();
            for (LR1Item item : currentState.items) {
                if (item.dotPosition < item.right.size()) {
                    symbols.add(item.right.get(item.dotPosition));
                }
            }

            // Pentru fiecare simbol, generează starea următoare.
            for (String symbol : symbols) {
                Set<LR1Item> nextItems = new HashSet<>();
                for (LR1Item item : currentState.items) {
                    if (item.dotPosition < item.right.size() && item.right.get(item.dotPosition).equals(symbol)) {
                        nextItems.add(new LR1Item(item.left, item.right, item.dotPosition + 1, item.lookahead));
                    }
                }

                // Calculează închiderea pentru starea nouă și adaug-o dacă nu există deja.
                State nextState = closure(nextItems, grammar);
                if (!states.contains(nextState)) {
                    states.add(nextState);
                    queue.add(nextState);
                }
                currentTransitions.put(symbol, nextState);
            }
        }
        return states;
    }

    private static State closure(Set<LR1Item> items, Grammar grammar) {
        // Calculează închiderea unui set de itemi LR(1).
        Set<LR1Item> closure = new HashSet<>(items);
        boolean changed;

        do {
            changed = false;
            Set<LR1Item> newItems = new HashSet<>();

            // Pentru fiecare item, adaugă noile itemi corespunzători neterminalelor.
            for (LR1Item item : closure) {
                if (item.dotPosition < item.right.size()) {
                    String symbol = item.right.get(item.dotPosition);
                    if (grammar.nonTerminals.contains(symbol)) {
                        for (Production production : grammar.productions) {
                            if (production.left.equals(symbol)) {
                                newItems.add(new LR1Item(production.left, production.right, 0, item.lookahead));
                            }
                        }
                    }
                }
            }

            // Adaugă noile itemi în închidere dacă sunt noi.
            if (closure.addAll(newItems)) changed = true;
        } while (changed);

        return new State(closure);
    }

    private static Map<State, Map<String, String>> generateActionTable(List<State> states, Grammar grammar, Map<State, Map<String, State>> transitions) {
        Map<State, Map<String, String>> actionTable = new HashMap<>();

        for (State state : states) {
            Map<String, String> actions = new HashMap<>();
            actionTable.put(state, actions);

            for (LR1Item item : state.items) {
                if (item.dotPosition == item.right.size()) {
                    // If the item is complete (dot is at the end), mark reduction.
                    if (item.left.equals(grammar.startSymbol) && item.lookahead.equals("$")) {
                        actions.put("$", "acc"); // Accept action.
                    } else {
                        // Find the production index for the reduction.
                        for (int i = 0; i < grammar.productions.size(); i++) {
                            Production production = grammar.productions.get(i);
                            if (production.left.equals(item.left) && production.right.equals(item.right)) {
                                actions.put(item.lookahead, "r" + i); // Reducere.
                                break;
                            }
                        }
                    }
                } else {
                    // If the item is not complete, mark shift.
                    String symbol = item.right.get(item.dotPosition);
                    if (grammar.terminals.contains(symbol)) {
                        State nextState = transitions.get(state).get(symbol);
                        if (nextState != null) {
                            actions.put(symbol, "s" + states.indexOf(nextState)); // Shift
                        }
                    }
                }
            }
        }

        return actionTable;
    }


    private static Map<State, Map<String, Integer>> generateGotoTable(List<State> states, Grammar grammar, Map<State, Map<String, State>> transitions) {
        Map<State, Map<String, Integer>> gotoTable = new HashMap<>();
        for (State state : states) {
            Map<String, Integer> gotos = new HashMap<>();
            gotoTable.put(state, gotos);

            for (String nonTerminal : grammar.nonTerminals) {
                State nextState = transitions.get(state).get(nonTerminal);
                if (nextState != null) {
                    gotos.put(nonTerminal, states.indexOf(nextState)); // Adaugă următoarea stare în tabelul de salt.
                }
            }
        }
        return gotoTable;
    }

    private static void printActionTable(List<State> states, Map<State, Map<String, String>> actionTable, List<String> terminals) {
        System.out.println("\nTabela de Acțiuni (TA):");

        System.out.printf("%-10s", "Stare");
        for (String terminal : terminals) {
            System.out.printf("%-10s", terminal);
        }
        System.out.println();

        for (int i = 0; i < states.size(); i++) {
            State state = states.get(i);
            System.out.printf("%-10d", i);
            for (String terminal : terminals) {
                String action = actionTable.getOrDefault(state, new HashMap<>()).getOrDefault(terminal, "-");
                System.out.printf("%-10s", action);
            }
            System.out.println();
        }
    }

    private static void printGotoTable(List<State> states, Map<State, Map<String, Integer>> gotoTable, List<String> nonTerminals) {
        System.out.println("\nTabela de Salt (TS):");

        System.out.printf("%-10s", "Stare");
        for (String nonTerminal : nonTerminals) {
            System.out.printf("%-10s", nonTerminal);
        }
        System.out.println();

        for (int i = 0; i < states.size(); i++) {
            State state = states.get(i);
            System.out.printf("%-10d", i);
            for (String nonTerminal : nonTerminals) {
                Integer nextState = gotoTable.getOrDefault(state, new HashMap<>()).getOrDefault(nonTerminal, -1);
                System.out.printf("%-10s", (nextState != -1 ? nextState : "-"));
            }
            System.out.println();
        }
    }
}
