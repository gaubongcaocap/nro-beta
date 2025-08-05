package boss.boss_manifest.Android;

import java.util.Random;

/*
 *
 *
 * @author Entidi (NTD - Tấn Đạt)
 */
import boss.Boss;
import boss.BossID;
import boss.BossesData;
import map.ItemMap;
import player.Player;
import services.PlayerService;
import services.Service;
import services.TaskService;
import utils.Util;

public class Android15 extends Boss {

    public boolean callApk13;

    public Android15() throws Exception {
        super(BossID.ANDROID_15, BossesData.ANDROID_15);
    }

    @Override
    public void reward(Player plKill) {
        // Check nhiệm vụ
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);

        // Drop vàng (item ID 190), số lượng ngẫu nhiên 200.000–3.000.000
        Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, 190, Util.nextInt(200000, 3000001),
                this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));

        int quantity = 1;
        ItemMap item1173 = new ItemMap(this.zone, 1173, quantity, this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
        Service.gI().dropItemMap(this.zone, item1173);

        if (Util.isTrue(80, 100)) {
            int[] items = Util.isTrue(50, 100) ? new int[]{18, 19, 20} : new int[]{1066, 1067, 1068, 1069, 1070, 1229};
            int randomItem = items[new Random().nextInt(items.length)];
            Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, randomItem, 1,
                    this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
        }
    }
    
    @Override
    protected void resetBase() {
        super.resetBase();
        this.callApk13 = false;
    }

    @Override
    public void active() {
        this.attack();
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.callApk13 && damage >= this.nPoint.hp) {
            if (this.parentBoss != null) {
                ((Android14) this.parentBoss).callApk13();
            }
            return 0;
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

    public void recoverHP() {
        PlayerService.gI().hoiPhuc(this, this.nPoint.hpMax, 0);
    }
}
