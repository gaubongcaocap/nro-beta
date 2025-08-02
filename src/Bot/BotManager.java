package Bot;

import java.util.ArrayList;
import java.util.List;
import server.ServerManager;

public class BotManager implements Runnable {

    public static BotManager i;
    
    public List<Bot> bot =  new ArrayList<>();
    
    
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
                Thread.sleep(150 - (System.currentTimeMillis() - st));
            } catch (Exception ignored) {
            }

        }
    }
}