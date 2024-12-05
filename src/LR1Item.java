import java.util.List;
import java.util.Objects;

public class LR1Item {
    String left; // Partea stângă
    List<String> right; // Partea dreaptă
    int dotPosition; // Poziția punctului
    String lookahead; // Simbolul de anticipare

    public LR1Item(String left, List<String> right, int dotPosition, String lookahead) {
        this.left = left;
        this.right = right;
        this.dotPosition = dotPosition;
        this.lookahead = lookahead;
    }

    @Override
    public boolean equals(Object obj) { //Două articole sunt egale dacă toate câmpurile coincid
        if (this == obj) return true;
        if (!(obj instanceof LR1Item)) return false;
        LR1Item other = (LR1Item) obj;
        return left.equals(other.left) &&
                right.equals(other.right) &&
                dotPosition == other.dotPosition &&
                lookahead.equals(other.lookahead);
    }

    @Override
    public int hashCode() { //Generare hash pentru utilizare în colecții (seturi)
        return Objects.hash(left, right, dotPosition, lookahead);
    }

    @Override
    public String toString() { // Format lizibil pentru articolul LR(1)
        return left + " → " + String.join("", right.subList(0, dotPosition)) + "." +
                String.join("", right.subList(dotPosition, right.size())) + ", " + lookahead;
    }
}
