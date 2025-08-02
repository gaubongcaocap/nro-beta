package player;

/*
 *
 *
 * @author Entidi (NTD - Tấn Đạt)
 */

import utils.Util;

public class ItemEvent {

    public Player player;

    public long lastTVGSTime;
    public long lastItemChuongDong;
    public long lastItemBanhQuy;
    public long lastItemCaTuyet;
    public long lastItemKeoDuong;
    public long lastItemKeoNguoiTuyet;
    public long lastItemManhVo;

    public int remainingChuongDongCount;
    public int remainingBanhQuyCount;
    public int remainingCaTuyetCount;
    public int remainingKeoDuongCount;
    public int remainingKeoNguoiTuyetCount;
    public int remainingTVGSCount;
    public int remainingManhVo;

    public long lastHHTime;

    public int remainingHHCount;

    public long lastBNTime;

    public int remainingBNCount;

    public ItemEvent(Player player) {
        this.player = player;
    }

    public boolean canDropTatVoGiangSinh(int maxCount) {
        if (Util.isAfterMidnight(lastTVGSTime)) {
            remainingTVGSCount = maxCount;
            lastTVGSTime = System.currentTimeMillis();
            return true;
        } else if (remainingTVGSCount > 0) {
            remainingTVGSCount--;
            return true;
        }
        return false;
    }

    public boolean canDropHoaHong(int maxCount) {
        if (Util.isAfterMidnight(lastHHTime)) {
            remainingHHCount = maxCount;
            lastHHTime = System.currentTimeMillis();
            return true;
        } else if (remainingHHCount > 0) {
            remainingHHCount--;
            return true;
        }
        return false;
    }

    public boolean canDropBinhNuoc(int maxCount) {
        if (Util.isAfterMidnight(lastBNTime)) {
            remainingBNCount = maxCount;
            lastBNTime = System.currentTimeMillis();
            return true;
        } else if (remainingBNCount > 0) {
            remainingBNCount--;
            return true;
        }
        return false;
    }

    public boolean canDropManhVo(int maxCount) {
        if (Util.isAfterMidnight(lastItemManhVo)) {
            remainingManhVo = maxCount;
            lastItemManhVo = System.currentTimeMillis();
            return true;
        } else if (remainingManhVo > 0) {
            remainingManhVo--;
            return true;
        }
        return false;
    }
}
