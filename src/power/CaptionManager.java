package power;

import java.sql.Connection;

import jdbc.DBConnecter;
import player.Player;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Entidi (NTD - Tấn Đạt)
 */
public class CaptionManager {

    private static final CaptionManager instance = new CaptionManager();

    public static CaptionManager getInstance() {
        return instance;
    }

    /**
     * List of loaded captions. Each caption corresponds to a power level and
     * contains text for different planets. This field was previously
     * annotated with Lombok's @Getter to generate an accessor. Since Lombok
     * is not used, an explicit getter method is provided below.
     */
    private List<Caption> captions;

    public CaptionManager() {
        captions = new ArrayList<>();
    }

    public void load() {
        try {
            try (Connection con = DBConnecter.getConnectionServer();) {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM `caption`");
                ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        int id = rs.getShort("id");
                        String earth = rs.getString("earth");
                        String saiya = rs.getString("saiya");
                        String namek = rs.getString("namek");
                        long power = rs.getLong("power");
                        Caption caption = Caption.builder()
                                .id(id)
                                .earth(earth)
                                .saiya(saiya)
                                .namek(namek)
                                .power(power)
                                .build();
                        add(caption);
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

    public void add(Caption caption) {
        captions.add(caption);
    }

    public void remove(Caption caption) {
        captions.remove(caption);
    }

    /**
     * Returns the list of captions. This method replaces the Lombok-generated
     * getter that used to exist when the field was annotated with @Getter.
     *
     * @return list of all loaded captions
     */
    public List<Caption> getCaptions() {
        return this.captions;
    }

    public Caption find(int id) {
        for (Caption caption : captions) {
            if (caption.getId() == id) {
                return caption;
            }
        }
        return null;
    }

    public Caption findLevel(int level) {
        for (int i = 0; i < captions.size(); i++) {
            if (i == level) {
                return captions.get(i);
            }
        }
        return null;
    }

    public int getLevel(Player player) {
        try {
            long power = player.nPoint.power;
            int size = captions.size();
            int level = 0;
            for (int i = size - 1; i >= 0; i--) {
                long p = captions.get(i).getPower();
                if (power >= p) {
                    level = i;
                    break;
                }
            }
            return level;
        } catch (Exception e) {

        }
        return 0;
    }
}
