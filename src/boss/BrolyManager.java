package boss;

/*
 *
 *
 * @author Entidi (NTD - Tấn Đạt)
 */

public class BrolyManager extends BossManager {

    private static BrolyManager instance;

    public static BrolyManager gI() {
        if (instance == null) {
            instance = new BrolyManager();
        }
        return instance;
    }

}
