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

public class Yamcha extends The23rdMartialArtCongress {

    public Yamcha(Player player) throws Exception {
        super(PHOBAN, BossID.YAMCHA, BossesData.YAMCHA);
        this.playerAtt = player;
    }
}
