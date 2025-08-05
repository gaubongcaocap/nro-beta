package boss.boss_manifest.Android;

import java.util.List;

/*
 *
 *
 * @author Entidi (NTD - Tấn Đạt)
 */

import boss.Boss;
import boss.BossID;
import boss.BossStatus;
import boss.BossesData;
import item.Item;
import map.ItemMap;
import player.Player;
import services.ItemService;
import services.Service;
import services.TaskService;
import utils.Util;

public class Poc extends Boss {

    public Poc() throws Exception {
        super(BossID.POC, BossesData.POC);
    }

    @Override
    public void reward(Player plKill) {
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
        int drop = 190; // 100% rơi item ID 190
        int quantity = Util.nextInt(20000, 30000);
        // Tạo itemMap cho item ID 190
        ItemMap itemMap = new ItemMap(this.zone, drop, quantity, x, y, plKill.id);
        ItemService.gI().createNewItem((short) drop);
        Service.gI().dropItemMap(zone, itemMap);

        ItemMap item1173 = new ItemMap(this.zone, 1173, 1, this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
        Service.gI().dropItemMap(this.zone, item1173);
        
        // 30% xác suất để rơi đồ
        if (Util.isTrue(10, 100)) {
            int group = Util.nextInt(1, 100) <= 70 ? 0 : 1;  // 70% chọn Áo Quần Giày (group = 0), 30% chọn Găng Rada (group = 1)

            // Các vật phẩm rơi từ nhóm Áo Quần Giày và Găng Rada
            int[][] drops = {
                {230, 231, 232, 234, 235, 236, 238, 239, 240, 242, 243, 244, 246, 247, 248, 250, 251, 252, 266, 267, 268, 270, 271, 272, 274, 275, 276}, // Áo Quần Giày
                {254, 255, 256, 258, 259, 260, 262, 263, 264, 278, 279, 280} // Găng Rada
            };

            // Chọn vật phẩm ngẫu nhiên từ nhóm đã chọn
            int dropOptional = drops[group][Util.nextInt(0, drops[group].length - 1)];

            // Tạo vật phẩm và thêm chỉ số shop
            ItemMap optionalItemMap = new ItemMap(this.zone, dropOptional, 1, x, y, plKill.id);
            ItemService.gI().createNewItem((short) dropOptional);
            List<Item.ItemOption> optionalOps = ItemService.gI().getListOptionItemShop((short) dropOptional);
            optionalOps.forEach(option -> option.param = (int) (option.param * Util.nextInt(100, 115) / 100.0));
            optionalItemMap.options.addAll(optionalOps);

            // Thêm chỉ số sao pha lê (80% từ 1-3 sao, 17% từ 4-5 sao, 3% sao 6)
            int rand = Util.nextInt(1, 100);
            int value = 0;
            if (rand <= 60) {
                value = Util.nextInt(0, 3); // 60%: 0–3 sao
            } else if (rand <= 97) {
                value = Util.nextInt(3, 6); // 3%: 3–6 sao
            } else if (rand <= 99) {
                value = Util.nextInt(7, 8); // 1%: 7–8 sao
            }

            optionalItemMap.options.add(new Item.ItemOption(107, value));

            // Drop vật phẩm tùy chọn xuống bản đồ
            Service.gI().dropItemMap(zone, optionalItemMap);
        }

        // 80% xác suất rơi ngọc rồng hoặc item cấp 2
        if (Util.isTrue(80, 100)) {
            int[] dropItems = {16, 17, 1150, 1151, 1152, 1152, 1066, 1067, 1068, 1069, 1070, 1229};
            int dropOptional = dropItems[Util.nextInt(0, dropItems.length - 1)];
            // Tạo và rơi vật phẩm ngọc rồng hoặc item cấp 2
            ItemMap optionalItemMap = new ItemMap(this.zone, dropOptional, 1, x, y, plKill.id);
            ItemService.gI().createNewItem((short) dropOptional);
            Service.gI().dropItemMap(zone, optionalItemMap);
        }      
        
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
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

//    @Override
//    public void wakeupAnotherBossWhenDisappear() {
//        if (this.parentBoss != null && !this.parentBoss.isDie()) {
//            this.parentBoss.changeToTypePK();
//        }
//    }
    @Override
    public void doneChatE() {
        if (this.parentBoss == null || this.parentBoss.bossAppearTogether == null
                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
            if (boss.id == BossID.PIC && !boss.isDie()) {
                boss.changeStatus(BossStatus.ACTIVE);
                break;
            }
        }
    }

}
