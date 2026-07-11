package jcommerce;

public class TokenManager {
    private static String cachedToken;
    private static long expiryTime;

    public static synchronized String getAdminToken() {
        if (cachedToken == null || expiresOn2min(expiryTime)) {
            cachedToken = Utils.getAdminJWT();
            expiryTime = nowPlus14min();
        }
        return cachedToken;
    }

    private static boolean expiresOn2min(long expiryTime){
        return System.currentTimeMillis() > expiryTime - 120_000;
    }

    private static long nowPlus14min(){
        return System.currentTimeMillis() + 900_000;
    }
}