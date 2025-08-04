package Bot;

import java.util.ArrayList;
import java.util.List;
import server.ServerManager;

public class BotManager implements Runnable {

    public static BotManager i;
    
    // Danh sách bot sử dụng CopyOnWriteArrayList để an toàn khi nhiều luồng thêm/xóa bot.
    public List<Bot> bot =  new java.util.concurrent.CopyOnWriteArrayList<>();
    
    private boolean started = false;
    
    public void start() {
        if(!started) {
            started = true;
            Thread.startVirtualThread(this);
        }
    }

    
    public static BotManager gI(){
        if(i == null){
            i = new BotManager();
        }
            return i;
    }
       @Override
    public void run() {
        while (ServerManager.isRunning) {
            try {
                long st = System.currentTimeMillis();
                for (Bot bot : this.bot) {
                    bot.update();
                }
                long delay = 150 - (System.currentTimeMillis() - st);
                // Đảm bảo thời gian ngủ luôn dương để tránh ném IllegalArgumentException
                if (delay < 10) {
                    delay = 10;
                }
                Thread.sleep(delay);
            } catch (Exception ignored) {
            }

        }
    }
}