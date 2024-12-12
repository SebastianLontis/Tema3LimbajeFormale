import java.util.*;

public class Grammar {// Utilizăm Grammar pentru a crea articole LR(1) și a le combina în stări (State)
    List<Production> productions = new ArrayList<>();
    Set<String> terminals = new HashSet<>();
    Set<String> nonTerminals = new HashSet<>();
    String startSymbol;

    public Grammar(String startSymbol) {
        this.startSymbol = startSymbol;
    }

    public void addProduction(String left, String right) {
        List<String> symbols = Arrays.asList(right.split("\\s+"));
        productions.add(new Production(left, symbols));
        nonTerminals.add(left);
        for (String symbol : symbols) {
            if (!symbol.matches("[A-Z]")) terminals.add(symbol);
            else nonTerminals.add(symbol);
        }
    }

    @Override
    public String toString() {
        return "Productions: " + productions +
                "\nNonTerminals: " + nonTerminals +
                "\nTerminals: " + terminals +
                "\nStart Symbol: " + startSymbol;
    }
}