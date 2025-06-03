package cracker;

public class Main {
    public static void main(String[] args) {
        String targetHash = HashUtil.sha256("79123456"); // Use real hash
        String[] prefixes = {"03", "70", "71", "76", "78", "79", "81", "82", "83", "84"};

        for (String prefix : prefixes) {
            for (int i = 0; i <= 999999; i++) {
                String candidate = prefix + String.format("%06d", i);
                System.out.println(candidate);
                if (HashUtil.sha256(candidate).equals(targetHash)) {
                    System.out.println("Password found: " + candidate);
                    return;
                }
            }
        }

        System.out.println("Password not found.");
    }
}
