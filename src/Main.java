import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Read the grammar from the file
        Grammar grammar = readGrammarFromFile("src/gramatica.txt");

        // Print out the grammar to verify it's loaded correctly
        System.out.println("Gramatică citită din fișier:");
        System.out.println("Simbol de start: " + grammar.startSymbol);
        System.out.println("Producții:");
        for (Production p : grammar.productions) {
            System.out.println(p);
        }

        // Generating states and transitions
        Map<State, Map<String, State>> transitions = new HashMap<>();
        List<State> states = generateStates(grammar, transitions);

        // Extract terminals and nonTerminals for table headers
        List<String> terminals = new ArrayList<>(grammar.terminals);
        terminals.add("$"); // Add end-of-input symbol
        List<String> nonTerminals = new ArrayList<>(grammar.nonTerminals);

        // Generating action and goto tables
        Map<State, Map<String, String>> actionTable = generateActionTable(states, grammar, transitions);
        Map<State, Map<String, Integer>> gotoTable = generateGotoTable(states, grammar, transitions);

        // Print action and goto tables
        printActionTable(states, actionTable, terminals);
        printGotoTable(states, gotoTable, nonTerminals);
    }

    // Method to read the grammar from the file
    private static Grammar readGrammarFromFile(String filename) throws IOException {
        Grammar grammar = new Grammar("E"); // Assuming "E" is the start symbol
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) {
                // Split the production into left and right parts
                String[] parts = line.split("->");
                String left = parts[0].trim();
                String right = parts[1].trim();

                // Add the production to the grammar
                grammar.addProduction(left, right);
            }
        }

        reader.close();
        return grammar;
    }

private static List<State> generateStates(Grammar grammar, Map<State, Map<String, State>> transitions) {
        // Generează stările și tranzițiile pentru gramatica dată
        //Construiește toate stările prin aplicarea funcțiilor de închidere și tranziție.
        //Utilizează o coadă pentru a genera stările în mod iterativ.
        List<State> states = new ArrayList<>();
        State startState = closure(Set.of(new LR1Item(grammar.startSymbol, List.of("E"), 0, "$")), grammar);
        states.add(startState);

        Queue<State> queue = new LinkedList<>();
        queue.add(startState);

        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            Map<String, State> currentTransitions = new HashMap<>();
            transitions.put(currentState, currentTransitions);

            Set<String> symbols = new HashSet<>();
            for (LR1Item item : currentState.items) {
                if (item.dotPosition < item.right.size()) {
                    symbols.add(item.right.get(item.dotPosition));
                }
            }

            for (String symbol : symbols) {
                Set<LR1Item> nextItems = new HashSet<>();
                for (LR1Item item : currentState.items) {
                    if (item.dotPosition < item.right.size() && item.right.get(item.dotPosition).equals(symbol)) {
                        nextItems.add(new LR1Item(item.left, item.right, item.dotPosition + 1, item.lookahead));
                    }
                }

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
        // Închide o colecție de articole LR(1) folosind regulile de producție din gramatică
        Set<LR1Item> closure = new HashSet<>(items);
        boolean changed;

        do {
            changed = false;
            Set<LR1Item> newItems = new HashSet<>();

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
                    if (item.left.equals(grammar.startSymbol)) {
                        actions.put("$", "acc");
                    } else {
                        actions.put(item.lookahead, "r" + grammar.productions.indexOf(new Production(item.left, item.right)));
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

            for (String symbol : grammar.nonTerminals) {
                if (transitions.get(state).containsKey(symbol)) {
                    gotos.put(symbol, states.indexOf(transitions.get(state).get(symbol)));
                }
            }
        }
        return gotoTable;
    }

    private static void printActionTable(List<State> states, Map<State, Map<String, String>> actionTable, List<String> terminals) {
        System.out.println("\nTabela de Acțiuni (TA):");

        // Print header
        System.out.print("Stare\t");
        for (String terminal : terminals) {
            System.out.print(terminal + "\t");
        }
        System.out.println();

        // Print rows
        for (int i = 0; i < states.size(); i++) {
            State state = states.get(i);
            System.out.print(i + "\t"); // State number
            for (String terminal : terminals) {
                String action = actionTable.getOrDefault(state, new HashMap<>()).getOrDefault(terminal, "-");
                System.out.print(action + "\t");
            }
            System.out.println();
        }
    }

    private static void printGotoTable(List<State> states, Map<State, Map<String, Integer>> gotoTable, List<String> nonTerminals) {
        System.out.println("\nTabela de Salt (TS):");

        // Print header
        System.out.print("Stare\t");
        for (String nonTerminal : nonTerminals) {
            System.out.print(nonTerminal + "\t");
        }
        System.out.println();

        // Print rows
        for (int i = 0; i < states.size(); i++) {
            State state = states.get(i);
            System.out.print(i + "\t"); // State number
            for (String nonTerminal : nonTerminals) {
                Integer nextState = gotoTable.getOrDefault(state, new HashMap<>()).get(nonTerminal);
                System.out.print((nextState != null ? nextState : "-") + "\t");
            }
            System.out.println();
        }
    }



}

