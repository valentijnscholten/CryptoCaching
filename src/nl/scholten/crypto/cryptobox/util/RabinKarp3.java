package nl.scholten.crypto.cryptobox.util;

public class RabinKarp3 {
    // Hashes that simply take the int value of a character
    public static int hash(char a) {
        return (int) a;
    }
    public static int hash(String a) {
        int hash = 0;
        for(char ch : a.toCharArray()) {
            hash += (int)ch;
        }
        return hash;
    }

    static public int strpos(String big, String small) {
        if(small.length() > big.length()) return -1;
        int desiredHash = hash(small);
        int actualHash = -1;
        char [] bigArr = big.toCharArray();
        for(int i=0; i<big.length(); i++) {
            if(i + small.length()  > big.length()) break;
            // Calculate hash once O(m)
            if(actualHash == -1) {
                actualHash = hash(big.substring(i, i+small.length()));
            }
            // Rolling hash, constant time on consecutive hashes
            else {
                actualHash -= hash(bigArr[i-1]);
                actualHash += hash(bigArr[i+small.length()-1]);
            }
            if(actualHash == desiredHash) {
                // Could have collided
                if(big.substring(i, i+small.length()).equals(small))
                    return i;
            }
        }
        return -1;
    }
}
