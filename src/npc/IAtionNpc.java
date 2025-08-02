package npc;

/*
 *
 *
 * @author Entidi (NTD - Tấn Đạt)
 */

import player.Player;

public interface IAtionNpc {

    void openBaseMenu(Player player);

    void confirmMenu(Player player, int select);

}
