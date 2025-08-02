package boss;

/*
 *
 *
 * @author Entidi (NTD - Tấn Đạt)
 */

public class FinalBossManager extends BossManager {

    private static FinalBossManager instance;

    public static FinalBossManager gI() {
        if (instance == null) {
            instance = new FinalBossManager();
        }
        return instance;
    }

}
