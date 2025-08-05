package power;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import jdbc.DBConnecter;
import lombok.Getter;

/**
 *
 * @author Entidi (NTD - Tấn Đạt)
 */

public class PowerLimitManager {

    private static final PowerLimitManager instance = new PowerLimitManager();

    public static PowerLimitManager getInstance() {
        return instance;
    }

    @Getter
    private List<PowerLimit> powers;

    public PowerLimitManager() {
        powers = new ArrayList<>();
    }

    public void load() {
        try {
            try (Connection con = DBConnecter.getConnectionServer();) {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM `power_limit`");
                ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        int id = rs.getShort("id");
                        long power = rs.getLong("power");
                        long hp = rs.getInt("hp");
                        long mp = rs.getInt("mp");
                        long damage = rs.getInt("damage");
                        int defense = rs.getInt("defense");
                        int critical = rs.getInt("critical");
                        PowerLimit powerLimit = PowerLimit.builder()
                                .id(id)
                                .power(power)
                                .hp(hp)
                                .mp(mp)
                                .damage(damage)
                                .defense(defense)
                                .critical(critical)
                                .build();
                        add(powerLimit);
                    }
                } finally {
                    rs.close();
                    ps.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void add(PowerLimit powerLimit) {
        powers.add(powerLimit);
    }

    public void remove(PowerLimit powerLimit) {
        powers.remove(powerLimit);
    }

    /**
     * Lấy giới hạn sức mạnh tương ứng với chỉ số limitPower.  Trong một số
     * trường hợp dữ liệu người chơi có thể lưu giá trị limitPower vượt ra ngoài
     * phạm vi mảng powers hoặc âm.  Thay vì trả về null khiến các hàm gọi phải
     * xử lý lỗi thủ công, phương thức sẽ trả về phần tử phù hợp nhất:
     *  - Nếu index < 0: trả về null (không tìm thấy)
     *  - Nếu index >= kích thước danh sách: trả về phần tử cuối cùng (giới hạn cao nhất)
     *  - Ngược lại: trả về phần tử tại vị trí index
     *
     * @param index vị trí limitPower mong muốn
     * @return Đối tượng PowerLimit tương ứng hoặc null nếu không tìm được
     */
    public PowerLimit get(int index) {
        if (powers == null || powers.isEmpty()) {
            return null;
        }
        if (index < 0) {
            return null;
        }
        if (index >= powers.size()) {
            // Trả về phần tử cuối cùng để đảm bảo luôn có giới hạn sử dụng
            return powers.get(powers.size() - 1);
        }
        return powers.get(index);
    }
}
