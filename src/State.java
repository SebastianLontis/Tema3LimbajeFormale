import java.util.HashSet;
import java.util.Set;

public class State { //O stare este o colecție de articole LR(1). Fiecare stare reprezintă un prefix viabil din gramatica procesată.
    Set<LR1Item> items; //// Mulțimea de articole LR(1) din care este formată starea

    public State(Set<LR1Item> items) { //
        this.items = new HashSet<>(items); //Stările folosesc articole unice
    }

    @Override
    public boolean equals(Object obj) {  // Două stări sunt egale dacă conțin aceleași articole
        if (this == obj) return true;
        if (!(obj instanceof State)) return false;
        State other = (State) obj;
        return items.equals(other.items);
    }

    @Override
    public int hashCode() {  // Generare hash pentru utilizare în colecții
        return items.hashCode();
    }

    @Override
    public String toString() {// Format lizibil pentru afișarea stării
        return "{" + items + "}";
    }
}

// Clasa este folosită pentru a construi AFD-ul.
//Reprezintă o stare a automatului finit determinist (AFD).