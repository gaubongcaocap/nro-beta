package boss.boss_manifest.NamekGinyuForce;

/*
 *
 *
 * @author EMTI
 */

import boss.Boss;
import boss.BossID;
import boss.BossStatus;
import boss.BossesData;
import item.Item;
import java.util.List;
import java.util.Random;
import services.Service;
import map.ItemMap;
import player.Player;
import services.ItemService;
import utils.Util;

public class SO3_NM extends Boss {

    private long st;

    public SO3_NM() throws Exception {
        super(BossID.SO_3_NM, false, true, BossesData.SO_3_NM);
    }

    @Override
    public void moveTo(int x, int y) {
        if (this.currentLevel == 1) {
            return;
        }
        super.moveTo(x, y);
    }

    @Override
    public void reward(Player plKill) {
        if (Util.isTrue(50, 100)) {
            int[] items = Util.isTrue(50, 100) ? new int[] { 18, 19, 20 }
                    : new int[] { 1066, 1067, 1068, 1069, 1070, 1229 };
            int randomItem = items[new Random().nextInt(items.length)];
            Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, randomItem, 1,
                    this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
        }
    }

    @Override
    protected void notifyJoinMap() {
        if (this.currentLevel == 1) {
            return;
        }
        super.notifyJoinMap();
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    @Override
    public void doneChatE() {
        if (this.parentBoss == null || this.parentBoss.bossAppearTogether == null
                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
            if ((boss.id == BossID.SO_2_NM || boss.id == BossID.SO_1_NM) && !boss.isDie()) {
                boss.changeStatus(BossStatus.ACTIVE);
                // break;
            }
        }
    }

}
