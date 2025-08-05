package boss.boss_manifest.Black;

/*
 *
 *
 * @author Entidi (NTD - Tấn Đạt)
 */
import boss.*;
import consts.ConstPlayer;
import consts.ConstTask;
import consts.ConstTaskBadges;
import map.ItemMap;
import player.Player;
import services.*;
import utils.Util;
import task.Badges.BadgesTaskService;

public class BlackGoku extends Boss {

    private long st;
    private int timeLeaveMap;

    public BlackGoku() throws Exception {
        super(BossID.BLACK_GOKU, false, true, BossesData.BLACK_GOKU, BossesData.SUPER_BLACK_GOKU);
    }

    @Override
    public void reward(Player plKill) {
        // Cập nhật thành tích săn boss
        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);

        // Kiểm tra nhiệm vụ TASK_31_0
        if (TaskService.gI().getIdTask(plKill) == ConstTask.TASK_31_0) {
            Service.gI().dropItemMap(this.zone, new ItemMap(zone, 992, 1, this.location.x, this.location.y, plKill.id));
            TaskService.gI().doneTask(plKill, ConstTask.TASK_31_0);
        }

        // 5% rơi đồ Thần Linh từ boss (có option 207)
        if (Util.isTrue(5, 100)) {
            ItemMap itemBoss = ItemService.gI().randDoTLBoss(this.zone, 1, this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, itemBoss);
        }

        // Tầng drop item 674 (1–3 món)
        if (Util.isTrue(5, 100)) {
            for (int i = 0; i < 3; i++) {
                Service.gI().dropItemMap(this.zone,
                        new ItemMap(this.zone, 674, 1, this.location.x, this.location.y, plKill.id));
            }
        } else if (Util.isTrue(20, 100)) {
            for (int i = 0; i < 2; i++) {
                Service.gI().dropItemMap(this.zone,
                        new ItemMap(this.zone, 674, 1, this.location.x, this.location.y, plKill.id));
            }
        } else if (Util.isTrue(30, 100)) {
            Service.gI().dropItemMap(this.zone,
                    new ItemMap(this.zone, 674, 1, this.location.x, this.location.y, plKill.id));
        }

        // Mưa item 1229 (5% tỷ lệ, 25–50 viên)
        if (Util.isTrue(5, 50)) {
            for (int i = 0; i < Util.nextInt(25, 50); i++) {
                ItemMap it = new ItemMap(this.zone, 1229, 1,
                        this.location.x + Util.nextInt(-15, 15),
                        this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
                Service.gI().dropItemMap(this.zone, it);
            }
        }

        // Rơi đá ID 77 từ 3–10 viên
        for (int i = 0; i < Util.nextInt(3, 10); i++) {
            Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, 77, Util.nextInt(10, 20),
                    this.location.x + i * 10,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
        }

        // Rơi vàng từ 10–20 triệu
        int gold = Util.nextInt(10_000_000, 20_000_000);
        Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, 190, gold,
                this.location.x + Util.nextInt(-30, 30),
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));

                
        ItemMap item1173 = new ItemMap(this.zone, 1173, 1, this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
        Service.gI().dropItemMap(this.zone, item1173);
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            if (this.currentLevel != 0) {
                damage /= 4;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage - Util.nextInt(100000));
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return damage;
        } else {
            return 0;
        }
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, timeLeaveMap)) {
            if (Util.isTrue(1, 2)) {
                this.leaveMap();
            } else {
                this.leaveMapNew();
            }
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
            timeLeaveMap = Util.nextInt(300000, 900000);
        }
    }

    @Override
    public void joinMap() {
        this.name = this.data[this.currentLevel].getName() + " " + Util.nextInt(1, 100);
        super.joinMap();
        st = System.currentTimeMillis();
        timeLeaveMap = Util.nextInt(600000, 900000);
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills
                        .get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                int dis = Util.getDistance(this, pl);
                if (dis > 450) {
                    move(pl.location.x - 24, pl.location.y);
                } else if (dis > 100) {
                    int dir = (this.location.x - pl.location.x < 0 ? 1 : -1);
                    int move = Util.nextInt(50, 100);
                    move(this.location.x + (dir == 1 ? move : -move), pl.location.y);
                } else {
                    if (Util.isTrue(30, 100)) {
                        int move = Util.nextInt(50);
                        move(pl.location.x + (Util.nextInt(0, 1) == 1 ? move : -move), this.location.y);
                    }
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
