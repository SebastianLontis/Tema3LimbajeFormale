import java.util.List;

public class Production { //Stochează informațiile despre o producție.

    String left; // Partea stângă (neterminalul)
    List<String> right; // Partea dreaptă (șirul de simboluri)

    public Production(String left, List<String> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return left + " -> " + String.join(" ", right); //returnează producția într-un format ușor de citit.
    }
}