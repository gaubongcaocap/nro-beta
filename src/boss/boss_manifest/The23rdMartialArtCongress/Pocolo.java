package boss.boss_manifest.The23rdMartialArtCongress;

/*
 *
 *
 * @author Entidi (NTD - Tấn Đạt)
 */

import boss.BossID;
import boss.BossesData;
import static boss.BossType.PHOBAN;
import player.Player;

public class Pocolo extends The23rdMartialArtCongress {

    public Pocolo(Player player) throws Exception {
        super(PHOBAN, BossID.PO_CO_LO, BossesData.POCOLO);
        this.playerAtt = player;
    }
}
