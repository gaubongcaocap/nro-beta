package boss;

/*
 *
 *
 * @author Entidi (NTD - Tấn Đạt)
 */

public class YardartManager extends BossManager {

    private static YardartManager instance;

    public static YardartManager gI() {
        if (instance == null) {
            instance = new YardartManager();
        }
        return instance;
    }

}
