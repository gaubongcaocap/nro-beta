package npc.npc_manifest;

/**
 *
 * @author Entidi (NTD - Tấn Đạt)
 */
import consts.ConstNpc;
import consts.cn;
import item.Item;
import jdbc.daos.PlayerDAO;
import npc.Npc;
import player.Player;
import services.NpcService;
import services.PetService;
import services.Service;
import services.TaskService;
import services.func.Input;
import shop.ShopService;

public class Bardock extends Npc {

    public Bardock(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                switch (mapId) {
                    case 0,7,14 ->
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Đây là nơi ngươi có tiền là có thể đổi bất cứ thứ gì"
                                + "\nNạp tiền hãy truy cập vào trang web: http://javhd.pro"
                                    + " \nNgươi đang có " + player.getSession().cash + " Coin",
                                "SHOP",
                                "Nạp Tiền",
                                " Mở thành viên",
                                "Đổi đệ VIP",
                                "Đổi thỏi vàng",
                                "Đổi skill đệ tử");
                    default ->
                        super.openBaseMenu(player);
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0:
                        ShopService.gI().opendShop(player, "BARDOCK_SHOP", false);
                        break;
                    case 4:
                        if (player.getSession() != null) {
                            this.createOtherMenu(player, 777,
                                    "Có tiền rồi đổi thôi!\n"
                                    + "Có thể nhận mốc nạp khi nạp ở quy lão Kame nha"
                                    + " \nBạn đang có :" + player.getSession().cash + " Coin",
                                    // "Đổi thỏi vàng");
                           "Đổi thỏi vàng", "Đổi ngọc xanh", "Đổi hồng ngọc");
                        }
                        break;
                    case 1:
                        NpcService.gI().createBigMessage(player, avartar, "Nhớ đăng nhập xong sau đó bấm NẠP!!!", (byte) 1, "NẠP", "http://javhd.pro");
                        break;
                    case 3:
                        if (player.getSession() != null) {                            
                            this.createOtherMenu(player, 13000,
                                    "Đại ca vừa trúng số à !!!!\n"
                                    + "Đổi đệ thì tháo cái đồ đệ ra, mất tự chịu nha!\n"
                                    + "Thông tin từng loại đệ:\n|2|"
                                    + "Fide nhí: +5%  chỉ số khi hợp thể\n|2|"
                                    + "Cell nhí: +10% chỉ số khi hợp thể\n|2|"
                                    + "Bưu nhí:  +15% chỉ số khi hợp thể\n|2|"
                                    + "Adr bãi biển: +20% chỉ số khi hợp thể\n|2|"
                                    + "Bưu gầy: +25% chỉ số khi hợp thể\n|2|"
                                    + "Berrus nhí: +30% chỉ số khi hợp thể\n"
                                    + "Bạn đang có :" + player.getSession().cash + " Coin",
                                    // + "\nChỉ số hợp thể đệ hiện tại :\n|7|"
                                    // + "-HP:" + player.pointfusion.getHpFusion() + " ,KI:" + player.pointfusion.getMpFusion() + " ,DAME:" + player.pointfusion.getDameFusion() + "",
                                    // "Mua đệ\nFide nhí\n<" + cn.de31 + ">", "Mua đệ\nCell nhí\n<" + cn.de32 + ">", "Mua đệ\nBưu nhí\n<" + cn.de33 + ">");
                                    "Mua đệ\nFide nhí\n" + cn.de31 + " Coin", "Mua đệ\nCell nhí\n" + cn.de32 + " Coin", "Mua đệ\nBưu nhí\n" + cn.de33 + " Coin", "Mua đệ\nAdr bãi biển\n" + cn.de34 + " Coin", "Mua đệ\nBưu gầy\n" + cn.de35 + " Coin", "Mua đệ\nBerrus nhí\n" + cn.de36 + " Coin");
                        }

                        Service.gI().sendThongBao(player, "Chức Năng chưa cập nhật ");
                        break;

                    case 2:
                        if (player.getSession() != null) {
                            this.createOtherMenu(player, 782,
                                    "Mở thành viên free khi bạn nạp tiền  \n Bạn đã nạp :"
                                    + "" + player.getSession().cash + " Coin\n"
                                    ,
                                    "Mở", "Đóng");
                        }
                        break;
                    case 5:
                        if (player.getSession() != null) {
                            this.createOtherMenu(player, 888,
                                    "Lưu ý: Đổi Skill đệ bằng tiền nạp sẽ mất Coin\n"
                                    + "Bạn có: " + player.getSession().cash + " Coin",
                                    //Menu CHọn
                                    "Đổi skill 2-3\n " + cn.skill23 + " Coin", "Đổi skill 2-4\n " + cn.skill24 + " Coin");

                        }
                        break;

                }
            } else if (player.iDMark.getIndexMenu() == 13000) {
                switch (select) {
                    case 0:
                        if (player.pet == null) {
                            Service.gI().sendThongBao(player, "Ngươi cần phải có đệ mới sử dụng được chức năng này?");
                            return;
                        }
                        for (Item item : player.pet.inventory.itemsBody) {
                            if (item.isNotNullItem()) {
                                Service.gI().sendThongBao(player, "Cần bỏ đồ đệ tử đang mặc để sử dụng chức năng?");
                                return;
                            }
                        }
                        if (player.getSession() != null && player.getSession().cash < cn.de31) {
                            Service.gI().sendThongBao(player, "Bạn không đủ " + cn.de31 + " Coin");
                            return;
                        }

                        if (PlayerDAO.subcash(player, cn.de31)) {
                            PetService.gI().createPetFideNhi(player, player.pet != null, player.gender);
                            Service.gI().sendThongBao(player, "Bạn đã nhận được đệ Fide Nhí");
                        } else {
                            Service.gI().sendThongBao(player, "Đã có lỗi xảy ra !!");
                        }

                        break;
                    case 1:
                        if (player.pet == null) {
                            Service.gI().sendThongBao(player, "Ngươi cần phải có đệ mới sử dụng được chức năng này?");
                            return;
                        }
                        for (Item item : player.pet.inventory.itemsBody) {
                            if (item.isNotNullItem()) {
                                Service.gI().sendThongBao(player, "Cần bỏ đồ đệ tử đang mặc để sử dụng chức năng?");
                                return;
                            }
                        }
                        if (player.getSession() != null && player.getSession().cash < cn.de32) {
                            Service.gI().sendThongBao(player, "Bạn không đủ " + cn.de32 + " Coin");
                            return;
                        }

                        if (PlayerDAO.subcash(player, cn.de32)) {
                            PetService.gI().createPetCellNhi(player, player.pet != null, player.gender);
                            Service.gI().sendThongBao(player, "Bạn đã nhận được đệ Cell Nhí");
                        } else {
                            Service.gI().sendThongBao(player, "Đã có lỗi xảy ra !!");
                        }
                        break;
                    case 2:
                        if (player.pet == null) {
                            Service.gI().sendThongBao(player, "Ngươi cần phải có đệ mới sử dụng được chức năng này?");
                            return;
                        }

                        for (Item item : player.pet.inventory.itemsBody) {
                            if (item.isNotNullItem()) {
                                Service.gI().sendThongBao(player, "Cần bỏ đồ đệ tử đang mặc để sử dụng chức năng?");
                                return;
                            }
                        }
                        if (player.getSession() != null && player.getSession().cash < cn.de33) {
                            Service.gI().sendThongBao(player, "Bạn không đủ " + cn.de33 + " Coin");
                            return;
                        }

                        if (PlayerDAO.subcash(player, cn.de33)) {
                            PetService.gI().createPetBuuNhi(player, player.pet != null, player.gender);
                            Service.gI().sendThongBao(player, "Bạn đã nhận được đệ Bưu Nhí");
                        } else {
                            Service.gI().sendThongBao(player, "Đã có lỗi xảy ra !!");
                        }

                        break;
                    case 3:
                        if (player.pet == null) {
                            Service.gI().sendThongBao(player, "Ngươi cần phải có đệ mới sử dụng được chức năng này?");
                            return;
                        }
                        for (Item item : player.pet.inventory.itemsBody) {
                            if (item.isNotNullItem()) {
                                Service.gI().sendThongBao(player, "Cần bỏ đồ đệ tử đang mặc để sử dụng chức năng?");
                                return;
                            }
                        }
                        if (player.getSession() != null && player.getSession().cash < cn.de34) {
                            Service.gI().sendThongBao(player, "Bạn không đủ " + cn.de34 + " Coin");
                            return;
                        }

                        if (PlayerDAO.subcash(player, cn.de34)) {
                            PetService.gI().createPetAdrBeach(player, player.pet != null, player.gender);
                            Service.gI().sendThongBao(player, "Bạn đã nhận được đệ Adr Bãi biển");
                        } else {
                            Service.gI().sendThongBao(player, "Đã có lỗi xảy ra !!");
                        }

                        break;
                    case 4:
                        if (player.pet == null) {
                            Service.gI().sendThongBao(player, "Ngươi cần phải có đệ mới sử dụng được chức năng này?");
                            return;
                        }
                        for (Item item : player.pet.inventory.itemsBody) {
                            if (item.isNotNullItem()) {
                                Service.gI().sendThongBao(player, "Cần bỏ đồ đệ tử đang mặc để sử dụng chức năng?");
                                return;
                            }
                        }
                        if (player.getSession() != null && player.getSession().cash < cn.de35) {
                            Service.gI().sendThongBao(player, "Bạn không đủ " + cn.de35 + " Coin");
                            return;
                        }

                        if (PlayerDAO.subcash(player, cn.de35)) {
                            PetService.gI().createPetMabuGay(player, player.pet != null, player.gender);
                            Service.gI().sendThongBao(player, "Bạn đã nhận được đệ Bưu Gầy");
                        } else {
                            Service.gI().sendThongBao(player, "Đã có lỗi xảy ra !!");
                        }
                        break;
                    case 5:
                        if (player.pet == null) {
                            Service.gI().sendThongBao(player, "Ngươi cần phải có đệ mới sử dụng được chức năng này?");
                            return;
                        }
                        for (Item item : player.pet.inventory.itemsBody) {
                            if (item.isNotNullItem()) {
                                Service.gI().sendThongBao(player, "Cần bỏ đồ đệ tử đang mặc để sử dụng chức năng?");
                                return;
                            }
                        }
                        if (player.getSession() != null && player.getSession().cash < cn.de36) {
                            Service.gI().sendThongBao(player, "Bạn không đủ " + cn.de36 + " Coin");
                            return;
                        }

                        if (PlayerDAO.subcash(player, cn.de36)) {
                            PetService.gI().createPetBerrusNhi(player, player.pet != null, player.gender);
                            Service.gI().sendThongBao(player, "Bạn đã nhận được đệ Berrus");
                        } else {
                            Service.gI().sendThongBao(player, "Đã có lỗi xảy ra !!");
                        }
                        break;

                }
            } else if (player.iDMark.getIndexMenu() == 888) {
                switch (select) {
                    case 0: //thay chiêu 2-3 đệ tử
                        if (player.getSession() != null && player.getSession().cash < cn.skill23) {
                            Service.gI().sendThongBao(player, "Bạn không đủ " + cn.skill23 + " Coin");
                            return;
                        }

                        if (PlayerDAO.subcash(player, cn.skill23)) {
                            if (player.pet != null) {
                                if (player.pet.playerSkill.skills.get(1).skillId != -1) {
                                    player.pet.openSkill2();
                                    if (player.pet.playerSkill.skills.get(2).skillId != -1) {
                                        player.pet.openSkill3();
                                    }
                                    Service.gI().sendThongBao(player, "Đổi skill 2-3 đệ thành công");
                                } else {
                                    Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu 2 chứ!");

                                }
                            } else {
                                Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");

                            }
                        }
                        break;
                    case 1: //thay chiêu 2-4 đệ tử
                        if (player.getSession() != null && player.getSession().cash < cn.skill24) {
                            Service.gI().sendThongBao(player, "Bạn không đủ " + cn.skill24 + " Coin");
                            return;
                        }

                        if (PlayerDAO.subcash(player, cn.skill24)) {
                            if (player.pet != null) {
                                if (player.pet.playerSkill.skills.get(1).skillId != -1) {
                                    player.pet.openSkill2();
                                    if (player.pet.playerSkill.skills.get(3).skillId != -1) {
                                        player.pet.openSkill4();
                                    }
                                    Service.gI().sendThongBao(player, "Đổi skill 2-4 đệ thành công");

                                } else {
                                    Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu 2 chứ!");

                                }
                            } else {
                                Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");

                            }
                        }
                        break;

                }
            } else if (player.iDMark.getIndexMenu() == 777) {
                switch (select) {
                    case 0:
                        Input.gI().createFormDoiThoiVang(player);
                        break;
                    case 1:
                        Input.gI().createFormDoiNgocXanh(player);
                        break;
                    case 2:
                        Input.gI().createFormDoiNgocHong(player);
                        break;
                }
            } else if (player.iDMark.getIndexMenu() == 782) {
                switch (select) {
                    case 0:
                       if (player.getSession() != null && player.getSession().actived) {
                           Service.gI().sendThongBao(player, "Bạn đã mở thành viên rồi");
                           return;
                       }
                        if (player.getSession() != null && player.getSession().danap < 1000) {
                            NpcService.gI().createBigMessage(player, avartar, "Bạn chưa nạp 1K, bạn có muốn nạp để mở thành viên không ?!!!", (byte) 1, "NẠP", "http://160.191.244.129/");
                            return;
                        }
                        if (PlayerDAO.updateActive(player, 1)) {
                            Service.gI().sendThongBao(player, "Bạn đã mở thành viên thành công");
                        } else {
                            Service.gI().sendThongBao(player, "Đã có lỗi xẩy ra khi kích hoạt tài khoản, vui long liên hệ admin nếu bị trừ tiền mà không kích hoạt được, chụp lại thông báo này");
                        }

                        break;
                    case 1:

                        break;

                }
            } else if (player.iDMark.getIndexMenu() == 0) {
                switch (mapId) {
                }
            }

        }
    }
}
