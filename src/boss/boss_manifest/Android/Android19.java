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
import skill.Skill;
import services.PlayerService;
import services.Service;
import services.TaskService;
import utils.Util;

public class Android19 extends Boss {

    public Android19() throws Exception {
        super(BossID.ANDROID_19, BossesData.ANDROID_19);
    }

    @Override
    public void reward(Player plKill) {
        // Cập nhật nhiệm vụ giết boss
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);

        // Drop vàng (ID 190), số lượng ngẫu nhiên từ 20.000–30.000
        Service.gI().dropItemMap(this.zone, new ItemMap(
            this.zone, 190, Util.nextInt(200000, 3000001),
            this.location.x,
            this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
            plKill.id));

        int quantity = 1;
        ItemMap item1743 = new ItemMap(this.zone, 1743, quantity, this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
        Service.gI().dropItemMap(this.zone, item1743);

        // 80% cơ hội drop item phụ (thường hoặc hiếm)
        if (Util.isTrue(80, 100)) {
            int[] items = Util.isTrue(50, 100)
                ? new int[]{18, 19, 20} // Thường
                : new int[]{1066, 1067, 1068, 1069, 1070, 1229}; // Hiếm

            int randomItem = items[new Random().nextInt(items.length)];

            Service.gI().dropItemMap(this.zone, new ItemMap(
                this.zone, randomItem, 1,
                this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                plKill.id));
        }

        // 10% cơ hội mưa quà (drop nhiều item 1229)
        if (Util.isTrue(5, 50)) {
            int dropCount = Util.nextInt(25, 50);
            for (int i = 0; i < dropCount; i++) {
                int offsetX = this.location.x + Util.nextInt(-15, 15);
                Service.gI().dropItemMap(this.zone, new ItemMap(
                    this.zone, 1229, 1,
                    offsetX,
                    this.zone.map.yPhysicInTop(offsetX, this.location.y - 24),
                    plKill.id));
            }
        }
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
    public void joinMap() {
        super.joinMap(); //To change body of generated methods, choose Tools | Templates.
        st = System.currentTimeMillis();
    }
    private long st;

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (plAtt != null) {
            switch (plAtt.playerSkill.skillSelect.template.id) {
                case Skill.KAMEJOKO:
                case Skill.MASENKO:
                case Skill.ANTOMIC:
                    long hpHoi =  ((long) damage * 80 / 100);
                    PlayerService.gI().hoiPhuc(this, hpHoi, 0);
                    if (Util.isTrue(1, 5)) {
                        this.chat("Hấp thụ.. các ngươi nghĩ sao vậy?");
                    }
                    return 0;
            }
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

    @Override
    public void wakeupAnotherBossWhenDisappear() {
        if (this.parentBoss != null) {
            this.parentBoss.changeToTypePK();
        }
    }

}
