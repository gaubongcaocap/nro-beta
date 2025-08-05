package boss.boss_manifest.Cell;

/*
 *
 *
 * @author Entidi (NTD - Tấn Đạt)
 */
import EMTI.Functions;
import consts.ConstPlayer;
import boss.Boss;
import boss.BossID;
import boss.BossStatus;
import boss.BossesData;
import consts.ConstTaskBadges;
import static event.EventManager.LUNNAR_NEW_YEAR;
import item.Item;
import java.util.List;
import map.ItemMap;
import player.Player;
import services.*;
import utils.Util;

import task.Badges.BadgesTaskService;

public class SieuBoHung extends Boss {

    private long st;
    public boolean callCellCon;

    private final String text[] = {"Thưa quý vị và các bạn, đây đúng là trận đấu trời long đất lở",
        "Vượt xa mọi dự đoán của chúng tôi",
        "Eo ơi toàn thân lão Xên bốc cháy kìa"};
    private long lastTimeChat;
    private long lastTimeMove;
    private int indexChat = 0;

    public SieuBoHung() throws Exception {
        super(BossID.SIEU_BO_HUNG, BossesData.SIEU_BO_HUNG_1, BossesData.SIEU_BO_HUNG_2);
    }

    @Override
    protected void resetBase() {
        super.resetBase();
        this.callCellCon = false;
    }

    public void callCellCon() {
        Thread.startVirtualThread(() -> {
            try {
                this.changeStatus(BossStatus.AFK);
                this.changeToTypeNonPK();
                this.recoverHP();
                this.callCellCon = true;
                this.chat("Hãy đấu với 7 đứa con của ta, chúng đều là siêu cao thủ");
                //Thread.sleep(2000);
                Functions.sleep(2000);
                this.chat("Cứ chưởng tiếp đi haha");
                //Thread.sleep(2000);
                Functions.sleep(2000);
                this.chat("Liệu mà giữ mạng đấy");
                //Thread.sleep(2000);
                Functions.sleep(2000);
                for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
                    switch ((int) boss.id) {
                        case BossID.XEN_CON_1 ->
                            boss.changeStatus(BossStatus.RESPAWN);
                        case BossID.XEN_CON_2 ->
                            boss.changeStatus(BossStatus.RESPAWN);
                        case BossID.XEN_CON_3 ->
                            boss.changeStatus(BossStatus.RESPAWN);
                        case BossID.XEN_CON_4 ->
                            boss.changeStatus(BossStatus.RESPAWN);
                        case BossID.XEN_CON_5 ->
                            boss.changeStatus(BossStatus.RESPAWN);
                        case BossID.XEN_CON_6 ->
                            boss.changeStatus(BossStatus.RESPAWN);
                        case BossID.XEN_CON_7 ->
                            boss.changeStatus(BossStatus.RESPAWN);
                    }
                }
            } catch (Exception e) {
            }
        });
    }

    public void recoverHP() {
        PlayerService.gI().hoiPhuc(this, this.nPoint.hpMax, 0);
    }

    @Override
    public void reward(Player plKill) {
        // Cập nhật nhiệm vụ huy hiệu săn boss
        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);

        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);

        // -------------------- RƠI VÀNG --------------------
        int goldId = 190;
        int goldQuantity = Util.nextInt(20_000, 30_000); // Rơi từ 20.000 đến 30.000 vàng
        ItemMap goldItem = new ItemMap(this.zone, goldId, goldQuantity, x, y, plKill.id);
        Service.gI().dropItemMap(zone, goldItem);

        ItemMap item1173 = new ItemMap(this.zone, 1173, 1, this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
        Service.gI().dropItemMap(this.zone, item1173);

        // -------------------- RƠI ĐỒ SỰ KIỆN (Tết Âm Lịch) --------------------
        if (LUNNAR_NEW_YEAR) {
            int eventItemId = 1475;
            ItemMap eventItem = new ItemMap(this.zone, eventItemId, 1, x, y, plKill.id);
            Service.gI().dropItemMap(zone, eventItem);
        }

        // -------------------- 30% RƠI ĐỒ CÓ OPTION NGẪU NHIÊN --------------------
        if (Util.isTrue(30, 100)) {
            int group = Util.nextInt(1, 100) <= 70 ? 0 : 1; // 70% là nhóm Áo/Quần/Giày, 30% là Găng/Rada
            int[][] drops = {
                {230, 231, 232, 234, 235, 236, 238, 239, 240, 242, 243, 244, 246, 247, 248, 250, 251, 252, 266, 267, 268, 270, 271, 272, 274, 275, 276}, // Nhóm 0
                {254, 255, 256, 258, 259, 260, 262, 263, 264, 278, 279, 280} // Nhóm 1
            };
            int dropId = drops[group][Util.nextInt(0, drops[group].length - 1)];

            // Tạo vật phẩm có option random
            ItemMap optionItem = new ItemMap(this.zone, dropId, 1, x, y, plKill.id);
            List<Item.ItemOption> options = ItemService.gI().getListOptionItemShop((short) dropId);
            options.forEach(opt -> opt.param = (int) (opt.param * Util.nextInt(100, 115) / 100.0)); // random param 100-115%
            optionItem.options.addAll(options);

            // Thêm chỉ số sao pha lê (option 107)
            int rand = Util.nextInt(1, 100);
            int crystalStar = 0;
            if (rand <= 80) crystalStar = Util.nextInt(1, 3); // 80%: 1-3 sao
            else if (rand <= 97) crystalStar = Util.nextInt(4, 5); // 17%: 4-5 sao
            else crystalStar = 6; // 3%: 6 sao
            optionItem.options.add(new Item.ItemOption(107, crystalStar));

            Service.gI().dropItemMap(zone, optionItem);
        }

        // -------------------- 80% RƠI NGỌC HOẶC ĐỒ CẤP 2 --------------------
        if (Util.isTrue(80, 100)) {
            int[] bonusItems = {16, 17, 1150, 1151, 1152, 1066, 1067, 1068, 1069, 1070, 1229};
            int dropId = bonusItems[Util.nextInt(0, bonusItems.length - 1)];
            int quantity2 = Util.nextInt(1, 3); // Rơi 1-2 cái
            ItemMap bonusItem = new ItemMap(this.zone, dropId, quantity2, x, y, plKill.id);
            Service.gI().dropItemMap(zone, bonusItem);
        }

        // -------------------- Cập nhật nhiệm vụ tiêu diệt boss --------------------
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (prepareBom) {
            return 0;
        }
        if (!this.callCellCon && damage >= this.nPoint.hp) {
            this.callCellCon();
            return 0;
        }
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage / 3);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = damage / 2;
            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                setBom(plAtt);
                return 0;
            }
            return damage;
        } else {
            return 0;
        }

    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        this.mc();
        if (this.currentLevel > 0) {
            if (this.bossStatus == BossStatus.AFK) {
                this.changeStatus(BossStatus.ACTIVE);
            }
        }
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    public void mc() {
        Player mc = zone.getNpc();
        if (mc != null) {
            if (Util.canDoWithTime(lastTimeChat, 3000)) {
                String textchat = text[indexChat];
                Service.gI().chat(mc, textchat);
                indexChat++;
                if (indexChat == text.length) {
                    indexChat = 0;
                    lastTimeChat = System.currentTimeMillis() + 7000;
                } else {
                    lastTimeChat = System.currentTimeMillis();
                }
            }

            if (Util.canDoWithTime(lastTimeMove, 15000)) {
                if (Util.isTrue(2, 3)) {
                    int x = this.location.x + Util.nextInt(-100, 100);
                    int y = x > 156 && x < 611 ? 288 : 312;
                    PlayerService.gI().playerMove(mc, x, y);
                }
                lastTimeMove = System.currentTimeMillis();
            }
        }
    }

}
