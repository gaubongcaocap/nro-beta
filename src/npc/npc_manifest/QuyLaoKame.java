package npc.npc_manifest;

/**
 *
 * @author Entidi (NTD - Tấn Đạt)
 */
import clan.Clan;
import consts.ConstNpc;
// import item.Item;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdbc.DBConnecter;
import models.TreasureUnderSea.TreasureUnderSea;
import models.TreasureUnderSea.TreasureUnderSeaService;
import npc.Npc;
import static npc.NpcFactory.PLAYERID_OBJECT;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.Archivement;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.RewardService;
import services.Service;
import services.TaskService;
import services.func.ChangeMapService;
import services.func.Input;
import shop.ShopService;
import utils.Util;

public class QuyLaoKame extends Npc {

    public QuyLaoKame(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        // Item ruacon = InventoryService.gI().findItemBag(player, 874);
        if (canOpenNpc(player)) {
            ArrayList<String> menu = new ArrayList<>();
//            if (!player.canReward) {
            menu.add("Nói\nchuyện");
            menu.add("Học Skill\nnăng mới");
            menu.add("Quà Mốc Nạp");
            menu.add("Hộp Thư");
//                menu.add("Bảng\n Xếp hạng\nNhiệm vụ");
//                if (ruacon != null && ruacon.quantity >= 1) {
//                    menu.add("Giao\nRùa con");
//                }
//            } else {
//                menu.add("Giao\nLân con");
//            }
            String[] menus = menu.toArray(String[]::new);
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Con muốn hỏi gì nào?", menus);
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.canReward) {
                RewardService.gI().rewardLancon(player);
                return;
            }
            switch (player.iDMark.getIndexMenu()) {
                case ConstNpc.BASE_MENU -> {
                    switch (select) {
                        case 0 -> {
                            ArrayList<String> menu = new ArrayList<>();
                            menu.add("Nhiệm vụ");
                            menu.add("Học\nKỹ năng");
                            Clan clan = player.clan;
                            if (clan != null) {
                                menu.add("Về khu\nvực bang");
                                if (clan.isLeader(player)) {
                                    menu.add("Giải tán\nBang hội");
                                }
                            }
                            menu.add("Kho báu\ndưới biển");
                            String[] menus = menu.toArray(String[]::new);

                            this.createOtherMenu(player, 0,
                                    "Chào con, ta rất vui khi gặp con\nCon muốn làm gì nào ?", menus);
                        }
                        case 1 -> {
//                            ShopService.gI().opendShop(player, "SKILL_NEW", true);
                            Service.gI().sendThongBao(player, "Chưa mở");
                        }
                        case 2 -> {
                            this.createOtherMenu(player, 1115, "Nạp đạt mốc nhận quà he :3", "Xem quà mốc nạp", "Nhận quà mốc nạp", "Đóng");
                        }
                        case 3 -> {
                            this.createOtherMenu(player, ConstNpc.MAIL_BOX,
                                    "|0|Tình yêu như một dây đàn\n"
                                    + "Tình vừa được thì đàn đứt dây\n"
                                    + "Đứt dây này anh thay dây khác\n"
                                    + "Mất em rồi anh biết thay ai?",
                                    "Hòm Thư\n(" + (player.inventory.itemsMailBox.size()
                                    - InventoryService.gI().getCountEmptyListItem(player.inventory.itemsMailBox))
                                    + " món)",
                                    "Xóa Hết\nHòm Thư", "Đóng");
                            break;
                        }
//                        case 4 -> {
//                            Item ruacon = InventoryService.gI().findItemBag(player, 874);
//                            if (ruacon != null && ruacon.quantity >= 1) {
//                                this.createOtherMenu(player, 1,
//                                        "Cảm ơn cậu đã cứu con rùa của ta\nĐể cảm ơn ta sẽ tặng cậu món quà.",
//                                        "Nhận quà", "Đóng");
//                                break;
//                            }
//                        }
                    }
                }
                case ConstNpc.MAIL_BOX -> {
                    switch (select) {
                        case 0:
//                                player.inventory.itemsMailBox.clear();
//                                player.inventory.itemsMailBox.addAll(GodGK.getMailBox(player));
                            ShopService.gI().opendShop(player, "ITEMS_MAIL_BOX", true);
                            break;
                        case 1:
                            NpcService.gI().createMenuConMeo(player,
                                    ConstNpc.CONFIRM_REMOVE_ALL_ITEM_MAIL_BOX, this.avartar,
                                    "|3|Bạn chắc muốn xóa hết vật phẩm trong hòm thư?\n"
                                    + "|7|Sau khi xóa sẽ không thể khôi phục!",
                                    "Đồng ý", "Hủy bỏ");
                            break;
                        case 2:
                            break;
                    }

                }

                case 1115 -> {
                    switch (select) {
                        case 0:

                            JSONArray dataArray;
                            JSONObject dataObject;
                            PreparedStatement ps = null;
                            ResultSet rs = null;
                            StringBuilder sb = new StringBuilder();
                            // sb.append("|0|__Reset mỗi thứ 2 đầu tuần__\n");
                            sb.append("|0|__Đang cập nhật__\n");
                            try ( Connection con2 = DBConnecter.getConnectionServer()) {
                                ps = con2.prepareStatement("SELECT * FROM moc_nap");
                                rs = ps.executeQuery();

                                // while (rs.next()) {
                                //     dataArray = (JSONArray) JSONValue.parse(rs.getString("detail"));
                                //     // sb.append("◥_____________________◤\n|7|");
                                //     sb.append("✎▶Mốc Nạp ").append(Archivement.GIADOLACHIADOI[rs.getInt("id") - 1])
                                //             .append("◀\n|0|");
                                //     sb.append("◢¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯◣\n|0|");

                                //     for (int i = 0; i < dataArray.size(); i++) {
                                //         dataObject = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(i)));
                                //         int tempid = Integer.parseInt(String.valueOf(dataObject.get("temp_id")));
                                //         int quantity = Integer.parseInt(String.valueOf(dataObject.get("quantity")));
                                //         JSONArray optionsArray = (JSONArray) dataObject.get("options");

                                //         sb.append("▷ x").append(quantity).append(" ")
                                //                 .append(ItemService.gI().getTemplate(tempid).name).append("\n|4|");

                                //         if (optionsArray != null) {
                                //             for (int j = 0; j < optionsArray.size(); j++) {
                                //                 JSONObject optionObject = (JSONObject) optionsArray.get(j);
                                //                 int optionId = Integer.parseInt(String.valueOf(optionObject.get("id")));
                                //                 int param = Integer.parseInt(String.valueOf(optionObject.get("param")));

                                //                 String optionTemplateName = ItemService.gI().getItemOptionTemplate(optionId).name;
                                //                 String formattedOption = optionTemplateName.replace("#", String.valueOf(param));

                                //                 sb.append(formattedOption).append("\n");
                                //             }
                                //         }
                                //         sb.append("\n|0|");
                                //     }
                                // }
                            } catch (SQLException ex) {
                                Logger.getLogger(QuyLaoKame.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            Service.gI().sendThongBaoFromAdmin(player, sb.toString());

                            break;
                        case 1:
                            if (player.getSession().actived) {
                                Archivement.gI().getAchievement(player);
                            } else {
                                Service.gI().sendThongBao(player, "Mở thành viên tại bardock đi rồi qua đây nhận nhe baby!");
                            }
                            break;
                        case 2:
                            break;
                    }
                }
                case 0 -> {
                    switch (select) {
                        case 0 ->
                            NpcService.gI().createTutorial(player, tempId, avartar, player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
                        case 1 ->
                            Service.gI().sendThongBao(player, "Bạn đã học hết các kỹ năng");
                        case 2 -> {
                            Clan clan = player.clan;
                            if (clan != null && select == 2) {
                                ChangeMapService.gI().changeMapNonSpaceship(player, 156, Util.nextInt(392, 400), 192);
                            } else {
                                if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                            "Bang hội con đang ở hang kho báu cấp "
                                            + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                            "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                            "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                            "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                                }
                            }
                        }
                        case 3 -> {
                            boolean clanCheck = true;
                            Clan clan = player.clan;
                            if (clan != null) {
                                clanCheck = false;
                                if (clan.isLeader(player)) {
                                    createOtherMenu(player, 3, "Con có chắc muốn giải tán bang hội không?", "Đồng ý", "Từ chối");
                                } else {
                                    clanCheck = true;
                                }
                            }
                            if (clanCheck) {
                                if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                            "Bang hội con đang ở hang kho báu cấp "
                                            + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                            "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                            "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                            "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                                }
                            }
                        }
                        case 4 -> {
                            if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                        "Bang hội con đang ở hang kho báu cấp "
                                        + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                        "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                            } else {
                                this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                        "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                        "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                            }
                        }
                    }
                }
                case 3 -> {
                    Clan clan = player.clan;
                    if (clan != null) {
                        if (clan.isLeader(player)) {
                            if (select == 0) {
                                Input.gI().createFormGiaiTanBangHoi(player);
                            }
                        }
                    }
                }
                case ConstNpc.MENU_OPENED_DBKB -> {
                    switch (select) {
                        case 2 -> {
                            if (player.clan == null) {
                                Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
                                return;
                            }
                            if (player.isAdmin() || player.nPoint.power >= TreasureUnderSea.POWER_CAN_GO_TO_DBKB) {
                                ChangeMapService.gI().goToDBKB(player);
                            } else {
                                this.npcChat(player, "Yêu cầu sức mạnh lớn hơn "
                                        + Util.numberToMoney(TreasureUnderSea.POWER_CAN_GO_TO_DBKB));
                            }
                        }

                    }
                }
                case ConstNpc.MENU_OPEN_DBKB -> {
                    switch (select) {
                        case 2 -> {
                            if (player.clan == null) {
                                Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
                                return;
                            }
                            if (player.isAdmin() || player.nPoint.power >= TreasureUnderSea.POWER_CAN_GO_TO_DBKB) {
                                Input.gI().createFormChooseLevelBDKB(player);
                            } else {
                                this.npcChat(player, "Yêu cầu sức mạnh lớn hơn "
                                        + Util.numberToMoney(TreasureUnderSea.POWER_CAN_GO_TO_DBKB));
                            }
                        }

                    }
                }
                case ConstNpc.MENU_ACCEPT_GO_TO_BDKB -> {
                    switch (select) {
                        case 0 ->
                            TreasureUnderSeaService.gI().openBanDoKhoBau(player, Byte.parseByte(String.valueOf(PLAYERID_OBJECT.get(player.id))));
                    }
                }
            }
        }
    }
}
