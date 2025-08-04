package map;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
public class MapManager {
    private static final String EFF_MAP_PATH = "data/map/eff_map/";
    private static final Map<Integer, List<EffectMap>> EFF_MAP_CACHE = new HashMap<>();

    // Load tất cả hiệu ứng map khi server khởi động
    public static void loadAllEffMaps() {
        File folder = new File(EFF_MAP_PATH);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Thư mục không tồn tại: " + EFF_MAP_PATH);
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.startsWith("effmap_") && name.endsWith(".ini"));
        Set<Integer> loadedMaps = new HashSet<>();

        if (files != null) {
            for (File file : files) {
                try {
                    int mapId = extractMapId(file.getName());
                    List<EffectMap> effectList = loadEffMapFromFile(file);

   
                    ensureBeff(effectList);

                    EFF_MAP_CACHE.put(mapId, effectList);
                    loadedMaps.add(mapId);
                } catch (NumberFormatException e) {
                    System.err.println("Lỗi khi lấy ID từ file: " + file.getName());
                } catch (IOException e) {
                    System.err.println("Lỗi khi đọc file: " + file.getName());
                    e.printStackTrace();
                }
            }
        }
        ensureAllMapsHaveBeff15(loadedMaps);
    }

    // Lấy hiệu ứng của map từ cache
    public static List<EffectMap> getEffMap(int mapId) {
        return EFF_MAP_CACHE.getOrDefault(mapId, Collections.emptyList());
    }

    // Đọc file hiệu ứng và parse JSON
    private static List<EffectMap> loadEffMapFromFile(File file) throws IOException {
        List<EffectMap> effectList = new ArrayList<>();

        String content = Files.readString(Path.of(file.getAbsolutePath()));
        JSONArray dataArray = (JSONArray) JSONValue.parse(content);
        if (dataArray == null) {
            return effectList;
        }

        for (Object obj : dataArray) {
            if (!(obj instanceof JSONArray dataItem) || dataItem.size() < 2) {
                continue;
            }

            EffectMap em = new EffectMap();
            em.setKey((String) dataItem.get(0));
            em.setValue((String) dataItem.get(1));
            effectList.add(em);
        }

        return effectList;
    }

    // Hàm đảm bảo luôn có ít nhất một hiệu ứng "beff" trong danh sách.  Một số
    // phiên bản server cũ chỉ kiểm tra giá trị "18" nên sẽ bỏ qua việc thêm
    // mặc định nếu gặp các giá trị khác (như 16 hoặc 17).  Sửa lại để chỉ cần
    // thấy key "beff" tồn tại là đủ; nếu không có bất kỳ hiệu ứng "beff",
    // thêm mới với giá trị "15".
    private static void ensureBeff(List<EffectMap> effectList) {
        for (EffectMap em : effectList) {
            if ("beff".equals(em.getKey())) {
                // Đã có một hiệu ứng beff bất kỳ, không cần thêm
                return;
            }
        }
        // Nếu danh sách chưa có bất kỳ hiệu ứng beff, thêm hiệu ứng mặc định
        EffectMap beff15 = new EffectMap();
        beff15.setKey("beff");
        beff15.setValue("15");
        effectList.add(beff15);
    }

    // Hàm đảm bảo mọi map đều có ít nhất một hiệu ứng beff, kể cả khi không có file.
    // Số lượng map tổng cộng được tính động dựa trên tập map đã tải thay vì cố định
    // một giá trị (như 200).  Nếu không có map nào được tải, giữ nguyên giá trị
    // mặc định 200 cho tính tương thích.
    private static void ensureAllMapsHaveBeff15(Set<Integer> loadedMaps) {
        // Xác định mapID lớn nhất đã được tải từ file.  Nếu không có map nào được
        // tải, sử dụng 200 như cũ để giữ tương thích với cấu trúc cũ.
        int maxLoadedId = 0;
        for (int id : loadedMaps) {
            if (id > maxLoadedId) {
                maxLoadedId = id;
            }
        }
        int totalMaps = maxLoadedId > 0 ? maxLoadedId : 200;
        for (int mapId = 1; mapId <= totalMaps; mapId++) {
            if (!loadedMaps.contains(mapId)) {
                List<EffectMap> effectList = new ArrayList<>();
                // Bổ sung hiệu ứng beff mặc định cho map chưa có file
                ensureBeff(effectList);
                EFF_MAP_CACHE.put(mapId, effectList);
            }
        }
    }

    // Hàm tách số từ tên file
    private static int extractMapId(String fileName) throws NumberFormatException {
        return Integer.parseInt(fileName.replaceAll("\\D+", ""));
    }
}