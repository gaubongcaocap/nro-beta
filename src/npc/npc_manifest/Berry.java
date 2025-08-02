package npc.npc_manifest;

import consts.ConstNpc;
import npc.Npc;
import player.Player;

public class Berry extends Npc {

    public Berry(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            createOtherMenu(player, ConstNpc.BASE_MENU, "max nhiệm vụ rồi chưa ra thêm!", "Đóng");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        // Không làm gì cả, vì Berry chỉ nói chuyện.
    }
}
