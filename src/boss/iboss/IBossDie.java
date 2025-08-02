package boss.iboss;

/*
 *
 *
 * @author Entidi (NTD - Tấn Đạt)
 */

import player.Player;

public interface IBossDie {

    void doSomeThing(Player playerKill);

    void notifyDie(Player playerKill);

    void rewards(Player playerKill);

    void leaveMap();

}
