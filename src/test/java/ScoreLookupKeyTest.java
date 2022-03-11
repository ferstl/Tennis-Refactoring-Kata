import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;


public class ScoreLookupKeyTest {

    @Test
    public void testHashCodeUnique() {
        Set<Integer> hashes = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                InitialGameState.ScoreLookupKey key = new InitialGameState.ScoreLookupKey(i, j);
                assertTrue(hashes.add(key.hashCode()));
            }

        }
    }

}
