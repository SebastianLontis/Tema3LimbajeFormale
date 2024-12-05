import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Definirea gramaticii
        Grammar grammar = new Grammar("E");
        grammar.addProduction("E", "E + T");
        grammar.addProduction("E", "T");
        grammar.addProduction("T", "T * F");
        grammar.addProduction("T", "F");
        grammar.addProduction("F", "( E )");
        grammar.addProduction("F", "id");

        // Generarea stărilor și tranzițiilor
        Map<State, Map<String, State>> transitions = new HashMap<>();
        List<State> states = generateStates(grammar, transitions);

        System.out.println("Stări:");
        for (int i = 0; i < states.size(); i++) {
            System.out.println("Stare " + i + ": " + states.get(i).items);
        }

        // Generarea tabelelor TA și TS
        Map<State, Map<String, String>> actionTable = generateActionTable(states, grammar, transitions);
        Map<State, Map<String, Integer>> gotoTable = generateGotoTable(states, grammar, transitions);

        System.out.println("\nTabela de Acțiuni:");
        printTable(actionTable);

        System.out.println("\nTabela de Salt:");
        printTable(gotoTable);
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

    private static void printTable(Map<?, ? extends Map<String, ?>> table) {
        for (var entry : table.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
